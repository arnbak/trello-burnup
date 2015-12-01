package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette, Environment }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms.LoginForm
import models.{ User, PageInfo }

import play.api.i18n.MessagesApi

import play.api.mvc._
import services.UserService

@Singleton
class LoginController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[User, CookieAuthenticator],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    avatarService: AvatarService,
    passwordHasher: PasswordHasher,
    socialProviderRegistry: SocialProviderRegistry) extends Silhouette[User, CookieAuthenticator] {

  def loginPage = UserAwareAction { implicit request =>
    Ok(views.html.login(PageInfo("Sign in", request.uri), LoginForm.form, socialProviderRegistry))
  }

  def create = Action { implicit request =>
    Redirect(routes.LoginController.createPage()).flashing("error" -> "That email address is already in use in this system!")
    //    LoginForm.form.bindFromRequest.fold(
    //      error => Future.successful(BadRequest(views.html.create(PageInfo("Create a user", request.uri), error))),
    //      success => {
    //        userService.findByEmail(Some(success.email)).flatMap { userOpt =>
    //          userOpt.map { user =>
    //            Future.successful(Redirect(routes.LoginController.createPage()).flashing("error" -> "That email address is already in use in this system!"))
    //          } getOrElse {
    //            userService.createUser(success.email, success.password).map { _ =>
    //              Redirect(routes.LoginController.loginPage()).flashing("success" -> "The user have been created, now go ahead and sign in with it!")
    //            }
    //          }
    //        }
    //      }
    //    )
  }

  def createPage = UserAwareAction { implicit request =>
    Ok(views.html.create(PageInfo("Create User", request.uri), LoginForm.form, socialProviderRegistry))
  }

  def login = Action { implicit request =>
    Redirect(routes.Application.dashboard()).flashing("success" -> "Welcome honored user.").withSession("email" -> "")
    //    LoginForm.form.bindFromRequest.fold(
    //      error => Future.successful(BadRequest(views.html.login(PageInfo("Sign in", request.uri), error))),
    //      success =>
    //        userService.validateUser(success.email, success.password).map { userOpt =>
    //          userOpt.map { u =>
    //            Redirect(routes.Application.dashboard()).flashing("success" -> "Welcome honored user.").withSession("email" -> u.email)
    //          } getOrElse {
    //            Redirect(routes.LoginController.loginPage()).flashing("error" -> "We could not sign you in, the user/password combination doesn't look right! Give it another try!")
    //          }
    //        }
    //    )
  }

  def logout = SecuredAction.async { implicit request =>
    //Redirect(routes.LoginController.loginPage()).withNewSession.flashing("success" -> "You are now signed out, thank you for using this application! Please come again!")

    val result = Redirect(routes.LoginController.loginPage())
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))

    env.authenticatorService.discard(request.authenticator, result)
  }

}