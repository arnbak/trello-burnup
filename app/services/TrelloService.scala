package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import models._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.joda.time
import org.joda.time.{ LocalDate, Days, DateTime }
import org.joda.time.format.DateTimeFormat
import play.Logger
import play.api.Configuration

import play.api.http.Status

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits._

@ImplementedBy(classOf[TrelloServiceImpl])
trait TrelloService {
  def member(user: User): Future[Member]
  def boardInfo(user: User, boards: List[DBBoard]): Future[List[Card]]
  def summarizeInfo(board: List[Card]): Future[Map[models.Point, models.Period]]

  def createBestLine(p: Period, finishedLine: List[Seq[Long]]): List[LineElement]
  def createFinishedLine(p: Period): List[Seq[Long]]
  def createScopeLine(p: Period): List[Seq[Long]]
}

class TrelloServiceImpl @Inject() (WS: WSClient,
    httpLayer: HTTPLayer,
    oAuth1InfoDAO: OAuth1InfoDAO,
    configuration: Configuration,
    boardService: BoardService) extends TrelloService {

  val settings = configuration.underlying.as[OAuth1Settings]("silhouette.trello")

  val oauthService = new PlayOAuth1Service(settings)

  def member(user: User): Future[Member] = {

    oAuth1InfoDAO.find(user.loginInfo).flatMap { infoOpt =>
      infoOpt.map { info =>

        httpLayer.url(s"https://api.trello.com/1/members/me?boards=all&organizations=all").sign(oauthService.sign(info)).get().flatMap { response =>

          response.status match {
            case Status.OK => Future.successful(response.json.as[Member])
            case _ => Future.failed(new Exception(s"Status from trello api ${response.status}"))
          }
        }
      }.getOrElse {
        Future.failed(new Exception("No login info was found"))
      }
    }
  }

  def boardInfo(user: User, boards: List[DBBoard]): Future[List[Card]] = {

    oAuth1InfoDAO.find(user.loginInfo).flatMap { infoOpt =>
      infoOpt.map { info =>

        val t = boards.map { board =>
          httpLayer.url(s"https://api.trello.com/1/boards/${board.id}/cards").sign(oauthService.sign(info)).get().flatMap { response =>
            response.status match {
              case Status.OK => Future.successful(response.json.as[List[Card]])
              case _ => Future.failed(new Exception(s"Status from trello api ${response.status}"))
            }
          }
        }

        Future.sequence(t).map { list => list.flatten }

      } getOrElse {
        Future.failed(new Exception("No login info was found"))
      }
    }
  }

  def summarizeInfo(board: List[Card]): Future[Map[models.Point, models.Period]] = Future.successful {

    board.groupBy(_.idBoard).map { boardCard =>

      val (boardId, boardCardList) = boardCard

      var points: Int = 0
      var finished: Int = 0
      var progress: Int = 0

      var startDate: Option[DateTime] = None
      var endDate: Option[DateTime] = None
      val fmt = DateTimeFormat.forPattern("ddMMyyyy")

      boardCardList.map { card =>
        Logger.info("Card " + boardCard._1 + " card " + card.name)

        val startIndex: Int = card.name.indexOf("[")
        val endIndex: Int = card.name.indexOf("]")

        if (startIndex != -1 && endIndex != -1) {
          //Logger.info("Name " + card.name)
          val name: String = card.name.substring(startIndex + 1, endIndex)
          //Logger.info("Name value " + name)

          try {
            val intValue = name.toInt
            points = points + intValue
            card.labels.map {
              label =>
                {
                  if (label.color.equals("blue")) {
                    progress = progress + intValue
                  } else if (label.color.equals("green")) {
                    finished = finished + intValue
                  } else if (label.color.equals("purple") || label.color.equals("red")) {
                    points = points - intValue
                  }
                }
            }
          } catch {
            case e: NumberFormatException => {
              if (name.equals("start")) {
                val start: String = card.name.substring(card.name.indexOf("]") + 1).trim
                startDate = Some(fmt.parseDateTime(start))
              } else if (name.equals("end")) {
                val end: String = card.name.substring(card.name.indexOf("]") + 1).trim
                endDate = Some(fmt.parseDateTime(end))
              }
            }
          }
        }
      }

      val validDates = for {
        sDate <- startDate
        eDate <- endDate
      } yield (sDate, eDate)

      val pointForToday: Point = Point(id = None, boardId, new time.DateTime(), points, progress, finished)
      val days: Option[Int] = Some(Days.daysBetween(validDates.get._1, validDates.get._2).getDays)
      val boardPeriod: Period = Period(id = None, boardId, validDates.get._1, validDates.get._2, days)

      (pointForToday, boardPeriod)

    }
  }

  def createBestLine(p: Period, finishedLine: List[Seq[Long]]): List[LineElement] = {

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

  def createFinishedLine(p: Period): List[Seq[Long]] = {
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

  def createScopeLine(p: Period): List[Seq[Long]] = {
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