package controllers

import models.{Users, User}
import play.api.mvc._
import play.api.db.slick._
import play.api.Play.current
import services.TrelloService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller with Secured {

  def dashboard = IsAuthedAsync { email => implicit request =>

    DB.withSession { implicit s =>
      Users.findByEmail(email).map { user =>
        val authUrl: Option[String] = Some("https://trello.com/1/authorize?key="+user.key.getOrElse("")+"&name=TrelloReleaseBurnUP&expiration=never&response_type=token&scope=read")

        val boardUrl = (for {
          key <- user.key
          token <- user.token
          if !token.isEmpty && !key.isEmpty
        } yield (key, token)).map { v =>
          "https://api.trello.com/1/members/me?key=" + v._1 + "&token=" + v._2 +"&boards=all&organizations=all"
        }

        for {
          boardsForUser <- TrelloService.member(boardUrl.get)
        } yield {
          Ok(views.html.index("Dashboard", Some(user), authUrl, Some(boardsForUser)))
        }

      } getOrElse {
        Future.successful(Ok(views.html.index("Dashboard", None, None, None)))
      }
    }
  }
}

trait Secured {

  private def email(request: RequestHeader) = request.session.get("email")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.LoginController.loginPage())

  def IsAuthenticated(f: => User => DBSessionRequest[AnyContent] => Result) = {
    Security.Authenticated(email, onUnauthorized) { email =>
      DBAction( implicit request =>
        Users.findByEmail(email).map {
          u => f(u)(request)
        } getOrElse {
          onUnauthorized(request)
        }
      )
    }
  }

  def IsAuthedAsync(f: => String => Request[_] => Future[Result]) = {
    Security.Authenticated(email, onUnauthorized) { email =>
      Action.async( implicit request =>
        f(email)(request)
      )
    }
  }

}
