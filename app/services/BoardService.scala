package services

import javax.inject.Singleton

import com.google.inject.{ Inject, ImplementedBy }
import models.{ Point, Period }
import org.joda.time.{ LocalDate, DateTime }

@ImplementedBy(classOf[BoardServiceImpl])
trait BoardService {
  def periodByBoardId(boardId: String): Option[Period]
  def pointForDay(boardId: String, day: LocalDate): Option[Point]
}

@Singleton
class BoardServiceImpl @Inject() () extends BoardService {

  def periodByBoardId(boardId: String) = {
    None
  }

  def pointForDay(boardId: String, day: LocalDate) = {
    None
  }
}