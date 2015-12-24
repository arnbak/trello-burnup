package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Logger
import com.mohiva.play.silhouette.api.services.AvatarService
import services.UserService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._

import models.{ PageInfo, User }

import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action

import scala.concurrent.Future

/**
 * The social auth controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info service implementation.
 * @param socialProviderRegistry The social provider registry.
 */
class SocialAuthController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[User, CookieAuthenticator],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    avatarService: AvatarService,
    socialProviderRegistry: SocialProviderRegistry) extends Silhouette[User, CookieAuthenticator] with Logger {

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = Action.async { implicit request =>

    def fetchAvatarImage(profile: CommonSocialProfile): Future[CommonSocialProfile] = {
      profile.email.map { email =>
        avatarService.retrieveURL(email).map { urlOpt =>
          profile.copy(avatarURL = urlOpt)
        }
      } getOrElse {
        Future.successful(profile)
      }
    }

    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) =>
            Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            profileWithAvatar <- fetchAvatarImage(profile)
            user <- userService.save(profileWithAvatar)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(profile.loginInfo)
            value <- env.authenticatorService.init(authenticator)
            result <- env.authenticatorService.embed(value, Redirect(routes.Application.dashboard()))
          } yield {
            env.eventBus.publish(LoginEvent(user, request, request2Messages))
            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.SocialAuthController.loginPage()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }

  def loginPage = UserAwareAction { implicit request =>
    Ok(views.html.login(PageInfo("Sign in", request.uri), socialProviderRegistry))
  }

  def logout = SecuredAction.async { implicit request =>
    //Redirect(routes.LoginController.loginPage()).withNewSession.flashing("success" -> "You are now signed out, thank you for using this application! Please come again!")

    val result = Redirect(routes.SocialAuthController.loginPage())
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))

    env.authenticatorService.discard(request.authenticator, result)
  }
}
