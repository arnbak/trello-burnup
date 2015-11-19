package services

import anorm._
import anorm.SqlParser._
import com.google.inject.{ ImplementedBy, Singleton, Inject }
import models.{ Point, Period, User }
import org.joda.time.DateTime
import play.api.db.Database

@ImplementedBy(classOf[DBBoardDao])
trait BoardDao {
  def findById(id: Long): Option[Period]
  def findPeriodByBordId(boardId: String): Option[Period]
  def insert(p: Period): Option[Period]

  def findAllPoints(): List[Point]
}

@Singleton
class DBBoardDao @Inject() (db: Database) extends BoardDao {
  import JodaParameterMetaData._
  private val periodParser = {
    get[Long]("id") ~
      get[String]("boardid") ~
      get[DateTime]("startdate") ~
      get[DateTime]("enddate") ~
      get[Option[Int]]("periodindays") map {
        case id ~ boardid ~ start ~ end ~ days => Period(id, boardid, start, end, days)
      }
  }

  private val pointParser = {
    get[Long]("id") ~
      get[String]("boardid") ~
      get[DateTime]("date") ~
      get[Int]("scope") ~
      get[Int]("progress") ~
      get[Int]("finished") map {
        case id ~ boardid ~ date ~ scope ~ progress ~ finished => Point(id, boardid, date, scope, progress, finished)
      }
  }

  def findPeriodByBordId(id: String) = {
    db.withConnection { implicit c =>
      SQL"""SELECT id,boardid,startdate,enddate,periodindays FROM public.boardperiods WHERE boardid=$id""".as(periodParser.singleOpt)
    }
  }

  def findById(id: Long) = {
    db.withConnection { implicit c =>
      SQL"""SELECT id,boardid,startdate,enddate,periodindays FROM public.boardperiods WHERE ID=$id""".as(periodParser.singleOpt)
    }
  }

  def insertPeriod(p: Period) = {
    db.withConnection { implicit c =>
      SQL"""INSERT INTO public.boardperiods (id,boardid,startdate,enddate,periodindays) VALUES (${p.boardId},${p.startDate},${p.endDate},${p.periodInDayes})""".executeInsert()
    } match {
      case Some(newId: Long) => findById(newId)
      case _ => None
    }
  }

  def updatePeriod(p: Period) = {
    db.withConnection { implicit c =>
      SQL"""UPDATE public.boardperiods SET startdate=${p.startDate},enddate=${p.endDate},periodindays=${p.periodInDayes} WHERE id=${p.id}""".executeUpdate()
    }
  }

  def insert(p: Period) = {
    val per = findPeriodByBordId(p.boardId)

    per.map { up =>
      updatePeriod(p)
      Some(p)
    } getOrElse {
      insertPeriod(p)
    }

  }

  def findAllPoints() = {
    db.withConnection { implicit c =>
      SQL"""SELECT id,boardid,date,scope,progress,finished FROM public.dailypoints""".as(pointParser.*)
    }
  }

  //def insert(period: Period)(implicit s: Session): Unit = {
  //    boardPeriods.filter{_.boardId === period.boardId}.firstOption.map { currentPeriod =>
  //
  //        val updatedPeriod: Period = Period(currentPeriod.id, period.boardId, period.startDate, period.endDate, period.periodInDayes)
  //
  //        boardPeriods.filter({ _.id === currentPeriod.id }).update(updatedPeriod)
  //
  //        Logger.debug("Updated period for board " + currentPeriod.boardId)
  //
  //    } getOrElse {
  //      boardPeriods.insert(period)
  //      Logger.debug("Inserted period for board " + period.boardId)
  //    }
  //  }

  //ef id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  //  def boardId = column[String]("BOARDID", O.NotNull)
  //  def startDate = column[Date]("STARTDATE", O.NotNull)
  //  def endDate = column[Date]("ENDDATE", O.NotNull)
  //  def periodInDays = column[Int]("PERIODINDAYS", O.NotNull)

}