package actors

import akka.actor.{ ActorLogging, Actor }
import com.google.inject.Inject
import models.User

import play.Logger
import services.{ BoardService, TrelloService, UserService }

import scala.concurrent.Future
import scala.concurrent.duration._

object AccumulationActor {
  case object Accumulate
  case class GetAccumulatableBoards(user: User)
  case class AccumulationMark(user: User, boardId: String)
}

class AccumulationActor @Inject() (userService: UserService,
    boardService: BoardService,
    trelloService: TrelloService) extends Actor with ActorLogging {

  import AccumulationActor._

  import context._

  val schedule = system.scheduler.schedule(2.seconds, 5.hours, self, Accumulate)

  def receive = {

    case Accumulate => {
      Logger.info("Accumulate")
      accumulate()
    }

    case _ => Logger.info("Unhandled Message")
  }

  def accumulate() = {

    val boardFuture = userService.allUsers.flatMap { userList =>

      val userBoards = userList.map { user =>

        trelloService.member(user).map { memberResult =>

          (user, memberResult)
        }
      }

      Future.sequence(userBoards)
    }

    val boardListFuture = boardService.listAllBoards

    val accumulatableList = for {
      memberList <- boardFuture
      boardList <- boardListFuture
    } yield {

      memberList.map { memberResult =>

        val (user, member) = memberResult

        val dbBoards = member.boards.flatMap { memberBoard =>
          boardList.filter(_.id == memberBoard.id)
        }

        (user, dbBoards)

      }
    }

    val elements = accumulatableList.flatMap { item =>
      Future.sequence {
        item.map { l =>
          val (user, boards) = l
          trelloService.boardInfo(user, boards)

        }
      }
    }

    val accumulatableBoardsFuture = elements.map { l => l.flatten }

    for {
      accumulatableBoards <- accumulatableBoardsFuture
      accumulatableInfo <- trelloService.summarizeInfo(accumulatableBoards)
    } yield {
      boardService.saveAccumulatedData(accumulatableInfo).map { i =>
        Logger.info("Accumulated")
      }
    }

  }

}
