package services

import javax.inject.Singleton

import com.google.inject.{ Inject, ImplementedBy }
import forms.AccumulateBoardForm
import models.{ DBBoard, Point, Period }
import org.joda.time.LocalDate
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future }

@ImplementedBy(classOf[BoardServiceImpl])
trait BoardService {
  def periodByBoardId(boardId: String): Future[Option[Period]]
  def pointForDay(boardId: String, day: LocalDate): Option[Point]
  def updateBoard(boards: List[AccumulateBoardForm]): Future[List[AccumulateBoardForm]]
  def findBoardById(boardId: String): Future[Option[DBBoard]]
  def listAllBoards: Future[List[DBBoard]]
  def listBoardsForAccumulation: Future[List[DBBoard]]
  def saveAccumulatedData(info: Map[Point, Period]): Future[Unit]
}

@Singleton
class BoardServiceImpl @Inject() (boardDao: BoardDao)(implicit ec: ExecutionContext) extends BoardService {

  def periodByBoardId(boardId: String) = Future.successful {
    boardDao.findPeriodByBordId(boardId)
  }

  def pointForDay(boardId: String, day: LocalDate) = {
    boardDao.findAllPoints().find(b => b.date.toLocalDate == day && b.boardId == boardId)
  }

  def saveAccumulatedData(info: Map[Point, Period]): Future[Unit] = {

    info.map { result =>
      for {
        pointRes <- boardDao.savePoint(result._1)
        periodRes <- boardDao.savePeriod(result._2)
      } yield {
        Logger.info("Saved ")
      }
    }

    Future.successful(Unit)

  }

  def updateBoard(boards: List[AccumulateBoardForm]) = {
    Future.successful {
      boards.flatMap { b =>
        boardDao.insertBoard(b.boardId, b.boardName, b.selected).map { m =>
          AccumulateBoardForm(m.id, m.name, m.selected)
        }
      }
    } recoverWith {
      case e: Throwable =>
        Logger.error("updateBoard : Error", e)
        Future.successful(List.empty)
    }
  }

  def findBoardById(boardId: String) = {
    Future.successful {
      boardDao.findBoardById(boardId)
    } recoverWith {
      case e: Throwable =>
        Logger.error("findBoardById : Error", e)
        Future.successful(None)
    }
  }

  def listAllBoards = Future.successful(boardDao.findAllBoards())

  def listBoardsForAccumulation = Future.successful { boardDao.findAllBoards().filter(_.selected == true) }

}