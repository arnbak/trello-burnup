package models

import org.joda.time.DateTime

case class Period(id: Long, boardId: String, startDate: DateTime, endDate: DateTime, periodInDayes: Option[Int] = Some(0))
case class Point(id: Long, boardId: String, date: DateTime, scope: Int, progress: Int, finished: Int)

//class BoardPeriods(tag: Tag) extends Table[Period](tag, "BOARDPERIODS") {
//  implicit val dateColumnType = MappedColumnType.base[Date, Long](d => d.getTime, d => new Date(d))
//
//  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
//  def boardId = column[String]("BOARDID", O.NotNull)
//  def startDate = column[Date]("STARTDATE", O.NotNull)
//  def endDate = column[Date]("ENDDATE", O.NotNull)
//  def periodInDays = column[Int]("PERIODINDAYS", O.NotNull)
//
//  def * = (id, boardId, startDate, endDate, periodInDays.?) <> (Period.tupled, Period.unapply _)
//}
//
//object BoardPeriods {
//
//  val boardPeriods = TableQuery[BoardPeriods]
//
//  def insert(period: Period)(implicit s: Session): Unit = {
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
//
//  def periodByBoardId(boardId: String) (implicit s: Session): Option[Period] = {
//    boardPeriods.filter(_.boardId === boardId).firstOption
//  }
//}
//
//
//
//class DailyPoints(tag: Tag) extends Table[Point](tag, "DAILYPOINTS") {
//
//  implicit val dateColumnType = MappedColumnType.base[Date, Long](d => d.getTime, d => new Date(d))
//
//  def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
//  def boardId = column[String]("BOARDID", O.NotNull)
//  def date = column[Date]("DATE", O.NotNull)
//  def scope = column[Int]("SCOPE", O.NotNull)
//  def progress = column[Int]("PROGRESS", O.NotNull)
//  def finished = column[Int]("FINISHED", O.NotNull)
//
//  def * = (id, boardId, date, scope, progress, finished) <> (Point.tupled, Point.unapply _)
//
//  implicit val pointFormat = Json.format[Point]
//}
//
//object DailyPoints  {
//
//  val dailyPoints = TableQuery[DailyPoints]
//
//  def insert(point: Point)(implicit s: Session): Unit = {
//
//    try {
//
//      dailyPoints.filter(p => p.boardId === point.boardId).list.map { p =>
//        val today: Boolean = new LocalDate(p.date).equals(new LocalDate())
//
//        if(today) {
//          dailyPoints.filter(_.id === p.id).delete
//        }
//      }
//      dailyPoints.insert(point)
//
//    } catch {
//      case e : Exception => {
//        Logger.error("Error " + e)
//      }
//    }
//
//
//  }
//
//  def pointForDay(boardId: String, day: LocalDate)(implicit s: Session): Option[Point] = {
//    val list: List[Point] = dailyPoints.filter({_.boardId === boardId}).list
//    list.find(p => (new LocalDate(p.date).equals(day)))
//  }
//
//  def count(implicit s: Session): Int = {
//    Query(dailyPoints.length).first
//  }
//
//  def countByBoardId(id: String)(implicit s: Session): Int = {
//    dailyPoints.filter{_.boardId === id}.list.length
//  }
//
//  def listByBoardId(id: String)(implicit s: Session): List[Point] = {
//    dailyPoints.filter{ _.boardId === id}.list
//  }
//
//}