package actors

import akka.actor.Actor
import com.google.inject.Inject

import play.Logger
import services.{ TrelloService, UserService }

import scala.concurrent.Future
import scala.concurrent.duration._

object AccumulationActor {
  case object Accumulate
}

class AccumulationActor @Inject() (userService: UserService, trelloService: TrelloService) extends Actor {

  import AccumulationActor._
  import context._

  val schedule = system.scheduler.schedule(6.minutes, 5.hours, self, Accumulate)

  def receive = {

    case Accumulate => {
      Logger.info("Accumulate")
      //accumulate()
    }

    case _ => Logger.info("Unhandled Message")
  }

  def accumulate() = {

    //    userService.allUsers.map { users =>
    //      users.foreach { user =>
    //
    //        val boardUrl = (for {
    //          key <- user.key
    //          token <- user.token
    //          if !token.isEmpty && !key.isEmpty
    //        } yield (key, token)).map { v =>
    //          val (key, token) = v
    //          s"https://api.trello.com/1/members/me?key=$key&token=$token&boards=all&organizations=all"
    //        }
    //
    //        Logger.info(s"Eventually ${user.key}, ${user.token}, ${boardUrl}")
    //
    //        //summarizeAndInsert(user.key, user.token, boardUrl)
    //      }
    //    }
  }

  //  def summarizeAndInsert(key: Option[String], token: Option[String], boardUrl: Option[String]): Future[Unit] = {
  //
  //    boardUrl.map { url =>
  //
  //      val memberFuture = trelloService.member(url)
  //
  //      for {
  //        member <- memberFuture
  //        cards <- trelloService.boardInfoFutures(key.get, token.get, member.idBoards)
  //      } yield {
  //
  //        cards.map { card =>
  //          Logger.info("Summerize")
  //          //val sum = trelloService.summarizeInfo(card)
  //
  //        }
  //      }
  //
  //      //res
  //
  //      //      (for {
  //      //        member <- trelloService.member(url)
  //      //        info <- trelloService.boardInfoFutures(key.get, token.get, member.idBoards)
  //      //      } yield info).map { cardList =>
  //      //        cardList.map { board =>
  //      //          trelloService.summarizeInfo(board).map { item =>
  //      //
  //      //
  //      ////            DB.withSession { implicit s: SlickSession =>
  //      ////              DailyPoints.insert(item._1)
  //      ////            }
  //      ////
  //      ////            DB.withSession { implicit s: SlickSession =>
  //      ////              BoardPeriods.insert(item._2)
  //      ////            }
  //      //
  //      //            Future.successful(item)
  //      //          }
  //      //        }
  //      //      }
  //      Future.successful()
  //    } getOrElse {
  //      Future.successful()
  //    }
  //  }

}
