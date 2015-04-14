package controllers
import models._
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.joda.time.{LocalDate, DateTime, Days}
import org.joda.time.format.{DateTimeFormat}

import play.{Logger}
import play.api.libs.json._
import play.api.mvc._

import play.api.libs.ws._

import play.api.db.slick._
import play.api.db.slick.{Session => SlickSession}
import play.api.Play.current
import play.api.libs.json.Json._
import services.TrelloService

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object TrelloController extends Controller with Secured {

  def accumulate = {

    DB.withSession { implicit s: SlickSession =>
      Users.listUsers.map { user =>

        val boardUrl = (for {
          key <- user.key
          token <- user.token
          if !token.isEmpty && !key.isEmpty
        } yield (key, token)).map { v =>
          s"https://api.trello.com/1/members/me?key=${v._1}&token=${v._2}&boards=all&organizations=all"
        }

        summarizeAndInsert(user.key, user.token, boardUrl)
      }
    }
  }

  def summarizeAndInsert(key: Option[String], token: Option[String], boardUrl: Option[String]): Future[Any] = {
    boardUrl.map { url =>

      (for {
        member <- TrelloService.member(url)
        info <- TrelloService.boardInfoFutures(key.get, token.get, member.idBoards)
      } yield (info)).map { cardList =>
        cardList.map { board =>

          TrelloService.summarizeInfo(board).map { item =>

            DB.withSession { implicit s: SlickSession =>
              DailyPoints.insert(item._1)
            }

            DB.withSession { implicit s: SlickSession =>
              BoardPeriods.insert(item._2)
            }

            Future.successful(item)
          }
        }
      }
    } getOrElse {
      Future.successful("Nothing")
    }
  }


  def accumulateToday = IsAuthedAsync { email => implicit request =>

    DB.withSession { implicit s =>
      Users.findByEmail(email).map { user =>
        Logger.info("Found user")

        val boardUrl = (for {
          key <- user.key
          token <- user.token
          if !token.isEmpty && !key.isEmpty
        } yield (key, token)).map { v =>
          s"https://api.trello.com/1/members/me?key=${v._1}&token=${v._2}&boards=all&organizations=all"
        }

        summarizeAndInsert(user.key, user.token, boardUrl).flatMap { _ =>
          Future.successful(Ok(Json.obj("message" -> s"accumulation triggered in the future")))
        }

      } getOrElse {
        Future.successful(Unauthorized(Json.obj("message" -> s"Authorization nedded")))
      }
    }
  }


  implicit  val periodFormat = Json.format[Period]

  def period(boardId: String) = IsAuthenticated { user => implicit request =>
    val period: Option[Period] = BoardPeriods.periodByBoardId(boardId)

    period.map {
      r => Ok(Json.toJson(r))
    } getOrElse {
      NotFound(Json.obj("message"-> ("no period found for boardid: " + boardId)))
    }
  }

  implicit val pointFormat = Json.format[Point]

  case class LineElement(x: Long, y: Double)

  object LineElement {

    implicit object LineElementFormat extends Format[LineElement] {

      def reads(json: JsValue) : JsSuccess[LineElement] = {

        val arr = json.as[JsArray]

        JsSuccess(LineElement(arr(0).as[Long],arr(1).as[Double]))
      }

      def writes(l: LineElement) : JsValue = {
        JsArray(Seq(JsNumber(l.x), JsNumber(l.y)))
      }
    }
  }


  def series(boardId: String) = IsAuthenticated { user => implicit request =>

    val period: Option[Period] = BoardPeriods.periodByBoardId(boardId)

    period.map { p =>

      val scopeLine: List[Seq[Long]] = createScopeLine(p)
      val finishedLine: List[Seq[Long]] = createFinishedLine(p)
      val bestLine: List[LineElement] = createBestLine(p, finishedLine)

      Ok(Json.obj("scopeLine" -> toJson(scopeLine),  "finishedLine" -> toJson(finishedLine), "bestLine" -> toJson(bestLine)))

    }.getOrElse {
      NotFound(Json.obj("message" -> ("no series found for boardid: " + boardId)))
    }

  }



  private def createBestLine(p: Period, finishedLine: List[Seq[Long]])(implicit s: SlickSession): List[LineElement] = {

    val regression = new SimpleRegression

    regression.addData(0, 0)

    finishedLine.zipWithIndex.foreach {
      case (v, i) => {
        regression.addData(i, v(1))
      }
    }

    val y = new ListBuffer[LineElement]

    for (i <- 0 to p.periodInDayes.get) {
      val day = new LocalDate(p.startDate).plusDays(i)
      y += LineElement(
        day.toDateTimeAtStartOfDay.getMillis,
        regression.getSlope * i
      )
    }

    y.toList

  }

  private def createFinishedLine(p: Period)(implicit s: SlickSession): List[Seq[Long]] = {
    val line = new ListBuffer[Seq[Long]]

    var finishedVal: Int = 0

    for (i <- 0 to p.periodInDayes.get) {
      val day = new LocalDate(p.startDate).plusDays(i)

      val point: Option[Point] = DailyPoints.pointForDay(p.boardId, day)

      point.map { p =>
        line += Seq(day.toDateTimeAtStartOfDay.getMillis, p.finished)
        finishedVal = p.finished
      }.getOrElse{
        if(finishedVal == 0) line += Seq(day.toDateTimeAtStartOfDay.getMillis, 0)
      }
    }

    line.toList
  }



  private def createScopeLine(p: Period)(implicit s: SlickSession): List[Seq[Long]] = {
    var line = new ListBuffer[Seq[Long]]
    var scopeVal: Int = 0

    for (i <- 0 to p.periodInDayes.get) {
      val day = new LocalDate(p.startDate).plusDays(i)

      val point: Option[Point] = DailyPoints.pointForDay(p.boardId, day)

      point.map { r =>
        line += Seq(day.toDateTimeAtStartOfDay.getMillis, r.scope)
        scopeVal = r.scope
      }.getOrElse {
        line += Seq(day.toDateTimeAtStartOfDay.getMillis,scopeVal)
      }
    }

    line.toList
  }
}