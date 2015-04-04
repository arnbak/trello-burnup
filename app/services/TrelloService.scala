package services

import java.util.Date

import models.{BoardPeriods, DailyPoints, Period, Point}
import org.joda.time.{Days, DateTime}
import org.joda.time.format.DateTimeFormat
import play.Logger

import play.api.http.Status

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.api.db.slick._

case class Member(id: String,
                  fullName: String,
                  initials: String,
                  memberType: String,
                  products: List[Int],
                  url: String,
                  username: String,
                  gravatarHash: String,
                  idBoards: List[String],
                  idOrganizations: Option[List[String]],
                  idBoardsPinned: Option[List[String]],
                  boards: List[Board])

object Member {

  implicit val memberReads: Reads[Member] = (
    (__ \ "id").read[String] and
      (__ \ "fullName").read[String] and
      (__ \ "initials").read[String] and
      (__ \ "memberType").read[String] and
      (__ \ "products").read[List[Int]] and
      (__ \ "url").read[String] and
      (__ \ "username").read[String] and
      (__ \ "gravatarHash").read[String] and
      (__ \ "idBoards").read[List[String]] and
      (__ \ "idOrganizations").read[Option[List[String]]] and
      (__ \ "idBoardsPinned").read[Option[List[String]]] and
      (__ \ "boards").read[List[Board]]
    )(Member.apply _)
}

case class Board(name: String,
                 closed: Boolean,
                 idOrganization: Option[String],
                 pinned: Option[String],
                 id: String)

object Board {
  implicit val boardReads: Reads[Board] = (
    (__ \ "name").read[String] and
      (__ \ "closed").read[Boolean] and
      (__ \ "idOrganization").readNullable[String] and
      (__ \ "pinned").readNullable[String] and
      (__ \ "id").read[String]
    )(Board.apply _)
}

case class Organization()

case class Card (
                  id: String,
                  closed: Boolean,
                  dateLastActivity: String,
                  desc: String,
                  name: String,
                  idBoard: String,
                  idList: String,
                  idShort: Int,
                  idLabels: List[String],
                  labels: List[Label],
                  shortUrl: String,
                  url: String)

object Card {
  implicit val cardReads: Reads[Card] = (
    (__ \ "id").read[String] and
      (__ \ "closed").read[Boolean] and
      (__ \ "dateLastActivity").read[String] and
      (__ \ "desc").read[String] and
      (__ \ "name").read[String] and
      (__ \ "idBoard").read[String] and
      (__ \ "idList").read[String] and
      (__ \ "idShort").read[Int] and
      (__ \ "idLabels").read[List[String]] and
      (__ \ "labels").read[List[Label]] and
      (__ \ "shortUrl").read[String] and
      (__ \ "url").read[String]
    )(Card.apply _)
}

case class Label(id: String,
                 idBoard: String,
                 name: String,
                 color: String,
                 uses: Int)

object Label {
  implicit val labelReads: Reads[Label] = (
    (__ \ "id").read[String] and
      (__ \ "idBoard").read[String] and
      (__ \ "name").read[String] and
      (__ \ "color").read[String] and
      (__ \ "uses").read[Int]
    )(Label.apply _)
}


object TrelloService {

  /**
   *
   * @param url
   * @return
   */
  def member(url: String): Future[Member] = {
    WS.url(url).get().flatMap { response =>
      response.status match {
        case Status.OK => Future.successful(response.json.as[Member])
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
      WS.url(cardsUrl).get().flatMap { response =>

        response.status match {
          case Status.OK => Future.successful(response.json.as[List[Card]])

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
  def summarizeInfo(board: List[Card]) : Map[models.Point, models.Period] = {

    val result = board.groupBy(_.idBoard).map { boardCard =>

      var points: Int = 0
      var finished: Int = 0
      var progress: Int = 0

      var startDate: Option[DateTime] = None
      var endDate: Option[DateTime] = None
      val fmt = DateTimeFormat.forPattern("ddMMyyyy")

      boardCard._2.map { card =>
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
              label => {
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


      val pointForToday: Point = Point(None, boardCard._1, new Date(), points, progress, finished)
      val days: Option[Int] = Some(Days.daysBetween(validDates.get._1, validDates.get._2).getDays)
      val boardPeriod: Period = Period(None, boardCard._1, validDates.get._1.toDate, validDates.get._2.toDate, days)


      (pointForToday, boardPeriod)

    }

    result
  }
}