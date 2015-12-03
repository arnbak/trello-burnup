package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import models._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.joda.time
import org.joda.time.{ Days, DateTime }
import org.joda.time.format.DateTimeFormat
import play.Logger
import play.api.Configuration

import play.api.http.Status

import scala.concurrent.Future
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits._

@ImplementedBy(classOf[TrelloServiceImpl])
trait TrelloService {
  def member(user: User): Future[Member]
}

class TrelloServiceImpl @Inject() (WS: WSClient, httpLayer: HTTPLayer, oAuth1InfoDAO: OAuth1InfoDAO, configuration: Configuration) extends TrelloService {

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
      }.get
    }
  }

  def boardInfoFutures(key: String, token: String, boards: List[String]): Future[List[List[Card]]] = {

    val futures = boards.map { boardId =>
      val cardsUrl = s"https://api.trello.com/1/boards/$boardId/cards?key=$key&token=$token"
      WS.url(cardsUrl).get().map { response =>
        response.status match {
          case Status.OK => response.json.as[List[Card]]

        }
      }
    }

    Future.sequence(futures)

  }

  def boardInfo(key: String, token: String, member: Member): Future[List[List[Card]]] = {

    val cardFutures = member.boards.map { board =>
      val cardUrl = s"https://api.trello.com/1/boards/${board.id}/cards?key=$key&token=$token"
      Logger.info(s"username: ${member.username} url $cardUrl")
      WS.url(cardUrl).get().map { response =>
        response.status match {
          case Status.OK => response.json.as[List[Card]]
        }
      }
    }

    Future.sequence(cardFutures).map { list =>
      list
    }
  }

  /**
   *
   * @param board
   * @return
   */
  def summarizeInfo(board: List[Card]): Map[models.Point, models.Period] = {

    val result = board.groupBy(_.idBoard).map { boardCard =>

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

      val pointForToday: Point = Point(-1, boardId, new time.DateTime(), points, progress, finished)
      val days: Option[Int] = Some(Days.daysBetween(validDates.get._1, validDates.get._2).getDays)
      val boardPeriod: Period = Period(-1, boardId, validDates.get._1, validDates.get._2, days)

      (pointForToday, boardPeriod)

    }

    result
  }
}