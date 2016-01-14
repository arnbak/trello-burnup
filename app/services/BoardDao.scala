package services

import javax.inject.{ Singleton, Inject }

import anorm._
import anorm.SqlParser._
import com.google.inject.ImplementedBy
import models.{ DBBoard, Point, Period }

import org.joda.time.DateTime
import play.api.Logger
import play.api.db.Database

import scala.util.{ Failure, Success, Try }

@ImplementedBy(classOf[DBBoardDao])
trait BoardDao {
  def findAllPoints(): List[Point]
  def findPeriodById(id: Long): Option[Period]
  def findPeriodByBordId(boardId: String): Option[Period]

  def savePoint(p: Point): Option[Point]
  def savePeriod(p: Period): Option[Period]

  def findAllBoards(): List[DBBoard]
  def findBoardById(boardId: String): Option[DBBoard]
  def insertBoard(boardId: String, boardName: String, selected: Boolean): Option[DBBoard]
}

@Singleton
class DBBoardDao @Inject() (db: Database) extends BoardDao {
  import JodaParameterMetaData._
  private val periodParser = {
    get[Option[Long]]("id") ~
      get[String]("boardid") ~
      get[DateTime]("startdate") ~
      get[DateTime]("enddate") ~
      get[Option[Int]]("periodindays") map {
        case id ~ boardid ~ start ~ end ~ days => Period(id, boardid, start, end, days)
      }
  }

  private val pointParser = {
    get[Option[Long]]("id") ~
      get[String]("boardid") ~
      get[DateTime]("date") ~
      get[Int]("scope") ~
      get[Int]("progress") ~
      get[Int]("finished") map {
        case id ~ boardid ~ date ~ scope ~ progress ~ finished => Point(id, boardid, date, scope, progress, finished)
      }
  }

  private val boardParser = {
    get[String]("boardId") ~
      get[String]("boardName") ~
      get[Boolean]("selected") ~
      get[DateTime]("updated") map {
        case boardId ~ boardName ~ selected ~ updated => DBBoard(boardId, boardName, selected, updated)
      }
  }

  def findPeriodByBordId(id: String) = {
    db.withConnection { implicit c =>
      SQL"""SELECT * FROM public.boardperiods WHERE boardid=$id""".as(periodParser.singleOpt)
    }
  }

  def findPeriodById(id: Long) = {
    db.withConnection { implicit c =>
      SQL"""SELECT * FROM public.boardperiods WHERE ID=$id""".as(periodParser.singleOpt)
    }
  }

  def findPointForToday(id: String) = {
    findAllPoints().find(p => p.boardId == id && p.date.toLocalDate == DateTime.now().toLocalDate)
  }

  def createPeriod(p: Period) = {
    db.withConnection { implicit c =>
      SQL"""INSERT INTO public.boardperiods (boardid,startdate,enddate,periodindays) VALUES (${p.boardId},${p.startDate},${p.endDate},${p.periodInDayes})""".executeInsert()
    } match {
      case Some(newId: Long) => findPeriodById(newId)
      case _ => None
    }
  }

  def updatePeriod(p: Period) = {
    db.withConnection { implicit c =>
      SQL"""UPDATE public.boardperiods SET startdate=${p.startDate},enddate=${p.endDate},periodindays=${p.periodInDayes} WHERE boardId=${p.boardId}""".executeUpdate()
    }
  }

  def updatePoint(p: Point) = {
    db.withConnection { implicit c =>
      SQL"""UPDATE public.dailypoints SET date=${p.date},scope=${p.scope},progress=${p.progress},finished=${p.finished} WHERE id=${p.id}""".executeUpdate()
    }
  }

  def createPoint(p: Point) = {
    db.withConnection { implicit c =>
      SQL"""INSERT INTO public.dailypoints (boardid,date,scope,progress,finished) VALUES (${p.boardId},${p.date},${p.scope},${p.progress},${p.finished})""".executeInsert()
    } match {
      case Some(newId: Long) => findPointById(newId)
      case _ => None
    }
  }

  def findPointById(id: Long) = {
    db.withConnection { implicit c =>
      SQL"SELECT * FROM public.dailypoints WHERE id=$id".as(pointParser.singleOpt)
    }
  }

  def savePoint(p: Point) = {

    findPointForToday(p.boardId) match {
      case Some(point) =>
        updatePoint(p.copy(id = point.id))
        Some(p)
      case None =>
        Logger.debug("Point for today not found, creating")
        createPoint(p)
    }
  }

  def savePeriod(p: Period) = {

    findPeriodByBordId(p.boardId) match {
      case Some(period) =>
        updatePeriod(p)
        Some(p)
      case None =>
        Logger.debug("Period not found, creating")
        createPeriod(p)
    }
  }

  def findAllPoints() = {
    db.withConnection { implicit c =>
      SQL"""SELECT id,boardid,date,scope,progress,finished FROM public.dailypoints""".as(pointParser.*)
    }
  }

  def findAllBoards() = {
    db.withConnection { implicit c =>
      SQL"SELECT * FROM public.boards".as(boardParser.*)
    }
  }

  def findBoardById(boardId: String) = {
    db.withConnection { implicit c =>
      SQL"SELECT * FROM public.boards WHERE boardId=$boardId".as(boardParser.singleOpt)
    }
  }

  def createBoard(boardId: String, boardName: String, selected: Boolean) = {
    db.withConnection { implicit c =>
      SQL"""INSERT INTO public.boards (boardId, boardName, selected) VALUES($boardId, $boardName, $selected)""".executeInsert()
    }

    findBoardById(boardId)
  }

  def updateBoard(boardId: String, boardName: String, selected: Boolean) = {
    db.withConnection { implicit c =>
      SQL"""UPDATE public.boards SET boardName=$boardName,selected=$selected,updated=${DateTime.now()} WHERE boardId=$boardId""".executeUpdate()
    } match {
      case updated: Int =>
        if (updated == 1) {
          findBoardById(boardId)
        } else {
          None
        }
    }
  }

  def insertBoard(boardId: String, boardName: String, selected: Boolean): Option[DBBoard] = {

    Try {
      findBoardById(boardId) match {
        case Some(board) => updateBoard(boardId, boardName, selected)
        case None => createBoard(boardId, boardName, selected)
      }
    } match {
      case Success(board) => board
      case Failure(e) =>
        Logger.info(s"E ${e.getMessage}")
        None
    }
  }
}