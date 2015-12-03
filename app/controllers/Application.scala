package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.{ User, PageInfo }
import play.api.i18n.MessagesApi
import services.{ OAuth1InfoDAO, TrelloService, UserService }
import scala.concurrent.ExecutionContext.Implicits._

class Application @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[User, CookieAuthenticator],
    userService: UserService,
    socialProviderRegistry: SocialProviderRegistry,
    trelloService: TrelloService,
    authInfo: OAuth1InfoDAO,
    httpLayer: HTTPLayer) extends Silhouette[User, CookieAuthenticator] {

  def dashboard = SecuredAction.async { implicit request =>
    trelloService.member(request.identity).map { memberResult =>
      Ok(views.html.index(PageInfo("Dashboard", request.uri), Some(request.identity), Some(memberResult)))
    }
  }

  def profile = SecuredAction { implicit request =>
    Ok(views.html.profile.profile(PageInfo("Profile", request.uri), user = Some(request.identity)))
  }

  def board(id: String) = SecuredAction.async { implicit request =>
    trelloService.member(request.identity).map { memberResult =>
      val board = memberResult.boards.find(_.id == id)
      Ok(views.html.boards.board(PageInfo("Board Info", request.uri), user = Some(request.identity), board))
    }
  }
}