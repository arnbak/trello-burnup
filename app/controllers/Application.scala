package controllers

import com.google.inject.{ Inject, Singleton }
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms.{ UserToken, UserKey }
import models.{ User, PageInfo }
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.{ TrelloService, UserService }

@Singleton
class Application @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[User, CookieAuthenticator],
    userService: UserService,
    socialProviderRegistry: SocialProviderRegistry,
    trelloService: TrelloService) extends Silhouette[User, CookieAuthenticator] {

  def dashboard = SecuredAction { implicit request =>

    //    val user = request.user
    //
    //    val authUrl: Option[String] = user.key.map { key =>
    //      s"https://trello.com/1/authorize?key=$key&name=TrelloReleaseBurnUP&expiration=never&response_type=token&scope=read"
    //    }
    //
    //    val boardUrl = (for {
    //      key <- user.key
    //      token <- user.token
    //      if !token.isEmpty && !key.isEmpty
    //    } yield (key, token)).map { v =>
    //      s"https://api.trello.com/1/members/me?key=${v._1}&token=${v._2}&boards=all&organizations=all"
    //    }
    //
    //    boardUrl.map { url =>
    //      trelloService.member(url).flatMap { m =>
    //        Future.successful(Ok(views.html.index(PageInfo("Dashboard", request.uri), Some(user), authUrl, Some(m))))
    //      }
    //    } getOrElse {
    //      Future.successful(Ok(views.html.index(PageInfo("Dashboard", request.uri), Some(user), authUrl, None)))
    //    }

    Ok(views.html.index(PageInfo("Dashboard", request.uri), user = Some(request.identity), None, None))
  }

  def keyForm = Action { implicit request =>
    Ok(views.html.fragments.keyform(PageInfo("Add trello key", request.uri), UserKey.form, user = None))
  }

  def saveKey = Action { implicit request =>
    //    UserKey.form.bindFromRequest().fold(
    //      error => Future.successful(BadRequest(views.html.fragments.keyform(PageInfo("Add trello key", request.uri), error, Some(request.user)))),
    //      success => {
    //        userService.addKey(request.user.email, success.key).map { userOpt =>
    //          userOpt.map { _ =>
    //            Redirect(routes.Application.dashboard()).flashing("success" -> "The key is saved, its time for you to generate the token!")
    //          } getOrElse {
    //            Redirect(routes.Application.saveKey()).flashing("error" -> "We could not save that key!")
    //          }
    //        }
    //      }
    //    )
    Redirect(routes.Application.dashboard()).flashing("success" -> "The key is saved, its time for you to generate the token!")
  }

  def tokenForm = Action { implicit request =>
    Ok(views.html.fragments.tokenform(PageInfo("Enter token", request.uri), UserToken.form, user = None))
  }

  def saveToken = Action { implicit request =>
    //    UserToken.form.bindFromRequest.fold(
    //      error => Future.successful(BadRequest(views.html.fragments.tokenform(PageInfo("Enter token", request.uri), error, user = Some(request.user)))),
    //      success => {
    //        userService.addToken(request.user.email, success.token).map { userOpt =>
    //          userOpt.map { _ =>
    //            Redirect(routes.Application.dashboard()).flashing("success" -> "The token is saved!")
    //          } getOrElse {
    //            Redirect(routes.Application.dashboard()).flashing("error" -> "We could not save the token!")
    //          }
    //        }
    //      }
    //    )

    Redirect(routes.Application.dashboard()).flashing("success" -> "The token is saved!")
  }

  def profile = SecuredAction { implicit request =>
    Ok(views.html.profile.profile(PageInfo("Profile", request.uri), user = Some(request.identity)))
  }

  def board(id: String) = Action { implicit request =>

    //    //
    //    val boardUrl = (for {
    //      key <- request.user.key
    //      token <- request.user.token
    //      if !token.isEmpty && !key.isEmpty
    //    } yield (key, token)).map { v =>
    //      s"https://api.trello.com/1/members/me?key=${v._1}&token=${v._2}&boards=all&organizations=all"
    //    }
    //    //
    //    boardUrl.map { url =>
    //      trelloService.member(url).flatMap { m =>
    //        Logger.info(s"Info $m")
    //        val boardOpt = m.boards.find(_.id == id)
    //        Future.successful(Ok(views.html.boards.index(PageInfo("Board Info", request.uri), Some(request.user), boardOpt)))
    //      }
    //    } getOrElse {
    //      Future.successful(Ok(views.html.boards.index(PageInfo("Board Info", request.uri), Some(request.user), None)))
    //    }

    Ok(views.html.boards.index(PageInfo("Board Info", request.uri), user = None, None))

  }

  //  class UserRequest[A](val user: Option[User], request: Request[A]) extends WrappedRequest[A](request)
  //
  //  object UserAwareAction extends ActionBuilder[UserRequest] with ActionTransformer[Request, UserRequest] {
  //    def transform[A](request: Request[A]) = {
  //
  //      for {
  //        email <- Future.successful(request.session.get("email"))
  //        user <- userService.findByEmail(email)
  //      } yield new UserRequest(user, request)
  //
  //    }
  //  }

  //  class RequestWithAuthedUser[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)
  //
  //  object UserAuthedAction extends ActionRefiner[UserRequest, RequestWithAuthedUser] {
  //    def refine[A](request: UserRequest[A]): Future[Either[Result, RequestWithAuthedUser[A]]] = Future.successful {
  //
  //      request.user.map { user =>
  //        new RequestWithAuthedUser(user, request)
  //      } toRight {
  //        //Sign in again?
  //        Redirect(routes.LoginController.login())
  //      }
  //    }
  //  }
  //
  //  val AuthedAction = UserAwareAction andThen UserAuthedAction
}