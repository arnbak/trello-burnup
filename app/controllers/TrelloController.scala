package controllers

import com.google.inject.{ Inject, Singleton }
import models._
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.joda.time.{ LocalDate, DateTime, Days }
import play.Logger

import play.api.libs.json._
import play.api.mvc._
import play.api.libs.json.Json._
import services.{ UserService, BoardService, TrelloService }

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class TrelloController @Inject() (
    trelloService: TrelloService,
    boardService: BoardService,
    userService: UserService)(implicit ec: ExecutionContext) extends Controller {

  def accumulateToday = Action { implicit request =>
    Ok
    //    DB.withSession { implicit s =>
    //      Users.findByEmail(email).map { user =>
    //        Logger.info("Found user")
    //
    //        val boardUrl = (for {
    //          key <- user.key
    //          token <- user.token
    //          if !token.isEmpty && !key.isEmpty
    //        } yield (key, token)).map { v =>
    //          s"https://api.trello.com/1/members/me?key=${v._1}&token=${v._2}&boards=all&organizations=all"
    //        }
    //
    //        summarizeAndInsert(user.key, user.token, boardUrl).flatMap { _ =>
    //          Future.successful(Ok(Json.obj("message" -> s"accumulation triggered in the future")))
    //        }
    //
    //      } getOrElse {
    //        Future.successful(Unauthorized(Json.obj("message" -> s"Authorization nedded")))
    //      }
    //    }
  }

  implicit val periodFormat = Json.format[Period]

  def period(boardId: String) = Action { implicit request =>
    val period: Option[Period] = boardService.periodByBoardId(boardId) //BoardPeriods.periodByBoardId(boardId)

    period.map {
      r => Ok(Json.toJson(r))
    } getOrElse {
      NotFound(Json.obj("message" -> ("no period found for boardid: " + boardId)))
    }
  }

  implicit val pointFormat = Json.format[Point]

  case class LineElement(x: Long, y: Double)

  object LineElement {

    implicit object LineElementFormat extends Format[LineElement] {

      def reads(json: JsValue): JsSuccess[LineElement] = {

        val arr = json.as[JsArray]

        JsSuccess(LineElement(arr(0).as[Long], arr(1).as[Double]))
      }

      def writes(l: LineElement): JsValue = {
        JsArray(Seq(JsNumber(l.x), JsNumber(l.y)))
      }
    }
  }

  def series(boardId: String) = Action { implicit request =>

    val period: Option[Period] = boardService.periodByBoardId(boardId)

    period.map { p =>

      val scopeLine: List[Seq[Long]] = createScopeLine(p)
      val finishedLine: List[Seq[Long]] = createFinishedLine(p)
      val bestLine: List[LineElement] = createBestLine(p, finishedLine)

      Ok(Json.obj("scopeLine" -> toJson(scopeLine), "finishedLine" -> toJson(finishedLine), "bestLine" -> toJson(bestLine)))

    }.getOrElse {
      NotFound(Json.obj("message" -> ("no series found for boardid: " + boardId)))
    }

  }

  private def createBestLine(p: Period, finishedLine: List[Seq[Long]]): List[LineElement] = {

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

  private def createFinishedLine(p: Period): List[Seq[Long]] = {
    val line = new ListBuffer[Seq[Long]]

    var finishedVal: Int = 0

    for (i <- 0 to p.periodInDayes.get) {
      val day = new LocalDate(p.startDate).plusDays(i)

      val point: Option[Point] = boardService.pointForDay(p.boardId, day)

      point.map { p =>
        finishedVal = p.finished
        line += Seq(day.toDateTimeAtStartOfDay.getMillis, p.finished)
      }.getOrElse {
        if (finishedVal == 0) line += Seq(day.toDateTimeAtStartOfDay.getMillis, 0)
      }
    }

    line.toList
  }

  private def createScopeLine(p: Period): List[Seq[Long]] = {
    var line = new ListBuffer[Seq[Long]]
    var scopeVal: Int = 0

    for (i <- 0 to p.periodInDayes.get) {
      val day = new LocalDate(p.startDate).plusDays(i)

      val point: Option[Point] = boardService.pointForDay(p.boardId, day)

      point.map { r =>
        scopeVal = r.scope
        line += Seq(day.toDateTimeAtStartOfDay.getMillis, r.scope)
      }.getOrElse {
        line += Seq(day.toDateTimeAtStartOfDay.getMillis, scopeVal)
      }
    }

    line.toList
  }
}