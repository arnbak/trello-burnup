package services

import com.google.inject.{ Inject, Singleton }
import models.{ Card, Member, Period, Point }
import org.joda.time
import org.joda.time.{ Days, DateTime }
import org.joda.time.format.DateTimeFormat
import play.Logger

import play.api.http.Status
import play.api.Play.current

import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.ws.WS

@Singleton
class TrelloService @Inject() ()(implicit ec: ExecutionContext) {

  /**
   *
   * @param url
   * @return
   */
  def member(url: String): Future[Member] = {

    WS.url(url).get().flatMap { response =>

      response.status match {
        case Status.OK => {
          Logger.info(s"Json Response ${response.json}")
          Future.successful(response.json.as[Member])
        }
      }
    }
  }

  /**
   *
   * @param key
   * @param token
   * @param boards
   * @return
   */
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

  /**
   *
   * @param key
   * @param token
   * @param member
   * @return
   */
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