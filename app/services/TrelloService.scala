package services

import java.text.DecimalFormat
import javax.inject.Inject
import com.google.inject.ImplementedBy
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import models._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.joda.time.{ Days, DateTime }
import org.joda.time.format.DateTimeFormat
import play.Logger
import play.api.Configuration

import play.api.http.Status

import scala.concurrent.Future
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{ Failure, Success, Try }

@ImplementedBy(classOf[TrelloServiceImpl])
trait TrelloService {
  def member(user: User): Future[Member]
  def boardInfo(user: User, boards: List[DBBoard]): Future[List[Card]]
  def summarizeInfo(board: List[Card]): Future[Map[models.Point, models.Period]]

  def createBestLine(p: Period, finishedLine: List[LineElement]): Future[List[LineElement]]
  def createFinishedLine(p: Period): Future[List[LineElement]]
  def createScopeLine(p: Period): Future[List[LineElement]]
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

  def parseBoardName(name: String): String = {
    val startIndex: Int = name.indexOf("[")
    val endIndex: Int = name.indexOf("]")

    if (startIndex != -1 && endIndex != -1) {
      name.substring(startIndex + 1, endIndex)
    } else {
      ""
    }
  }

  def parseNameAsValue(name: String): Option[Int] = {
    Try {
      val boardNameValue = parseBoardName(name)
      boardNameValue.toInt
    } match {
      case Success(intValue) => Some(intValue)
      case Failure(_) => None
    }
  }

  def summarizeInfo(board: List[Card]): Future[Map[models.Point, models.Period]] = Future.successful {

    board.groupBy(_.idBoard).map {
      case (boardId, boardCardList) =>

        var scopePoints: Int = 0
        var finished: Int = 0
        var progress: Int = 0

        val fmt = DateTimeFormat.forPattern("ddMMyyyy")

        val startDate: Option[DateTime] = boardCardList.find(c => c.name.contains("start")).map { c =>
          val start: String = c.name.substring(c.name.indexOf("]") + 1).trim
          fmt.parseDateTime(start)
        }

        val endDate: Option[DateTime] = boardCardList.find(c => c.name.contains("end")).map { c =>
          val end: String = c.name.substring(c.name.indexOf("]") + 1).trim
          fmt.parseDateTime(end)
        }

        boardCardList.foreach { card =>

          parseNameAsValue(card.name) match {
            case Some(intValue) =>

              scopePoints = scopePoints + intValue

              card.labels.foreach { label =>
                label.color match {
                  case "blue" => progress = progress + intValue
                  case "green" => finished = finished + intValue
                  case "purple" => scopePoints = scopePoints - intValue
                  case "red" => scopePoints = scopePoints - intValue
                  case _ => Logger.info(s"Color `${label.color}` not taken into account")
                }
              }

            case _ =>
          }
        }

        val validDates = for {
          sDate <- startDate
          eDate <- endDate
        } yield (sDate, eDate)

        val pointForToday: Point = Point(id = None, boardId, new DateTime(), scopePoints, progress, finished)
        val days: Option[Int] = Some(Days.daysBetween(validDates.get._1, validDates.get._2).getDays)
        val boardPeriod: Period = Period(id = None, boardId, validDates.get._1, validDates.get._2, days)

        (pointForToday, boardPeriod)

    }
  }

  def createBestLine(p: Period, finishedLine: List[LineElement]): Future[List[LineElement]] = Future.successful {

    val regression = new SimpleRegression

    regression.addData(0, 0)

    finishedLine.zipWithIndex.foreach {
      case (v, i) => regression.addData(i, v.y)
    }

    val df = new DecimalFormat("#.0")

    p.periodInDayes.fold(List.empty[LineElement]) { days =>

      (for {
        i <- 0 to days
      } yield {
        LineElement(
          p.startDate.toLocalDate.plusDays(i).toDateTimeAtStartOfDay.getMillis,
          df.format(regression.getSlope * i).toDouble
        )
      }).toList
    }
  }

  def createFinishedLine(p: Period): Future[List[LineElement]] = {

    val vS = p.periodInDayes.fold(List.empty[LineElement]) { days =>

      var finishedVal: Int = 0

      val v = (for {
        i <- 0 to days
      } yield {
        p.startDate.plusDays(i).toLocalDate
      }).zipWithIndex.map {
        case (d, i) =>

          if (i == 0) {
            LineElement(d.toDateTimeAtStartOfDay.getMillis, 0)
          } else {
            boardService.pointForDay(p.boardId, d).map { period =>
              finishedVal = period.finished
              LineElement(d.toDateTimeAtStartOfDay.getMillis, period.finished)
            } getOrElse {
              LineElement(d.toDateTimeAtStartOfDay.getMillis, finishedVal)
            }
          }
      }

      v.toList

    }

    Future.successful(vS)
  }

  def createScopeLine(p: Period): Future[List[LineElement]] = Future.successful {
    p.periodInDayes.fold(List.empty[LineElement]) { days =>
      var scopeVal: Int = 0

      (for {
        i <- 0 to days
      } yield {
        p.startDate.plusDays(i).toLocalDate
      }).map { t =>
        boardService.pointForDay(p.boardId, t).map { p =>
          scopeVal = p.scope
          LineElement(t.toDateTimeAtStartOfDay.getMillis, p.scope)
        } getOrElse {
          LineElement(t.toDateTimeAtStartOfDay.getMillis, scopeVal)
        }
      }.toList
    }
  }
}