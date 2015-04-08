package controllers

import models.{PageInfo, Users}

import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.db.slick._
import services.TrelloService
import scala.concurrent.Future
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object BoardController extends Controller with Secured {

  def board(id: String) = IsAuthedAsync { email => implicit request =>

    DB.withSession { implicit s =>
      Users.findByEmail(email).map { user =>

        val boardUrl = (for {
          key <- user.key
          token <- user.token
          if !token.isEmpty && !key.isEmpty
        } yield (key, token)).map { v =>
          s"https://api.trello.com/1/members/me?key=${v._1}&token=${v._2}&boards=all&organizations=all"
        }

        boardUrl.map { url =>
          TrelloService.member(url).flatMap { m =>
            val boardOpt = m.boards.find(_.id == id)
            Future.successful(Ok(views.html.boards.index(PageInfo("Board Info", request.uri), Some(user), boardOpt)))
          }
        } getOrElse {
          Future.successful(Ok(views.html.boards.index(PageInfo("Board Info", request.uri), Some(user), None)))
        }
      } getOrElse {
        Future.successful(Unauthorized(Json.obj("message" -> s"Authorization nedded")))
      }
    }
  }
}