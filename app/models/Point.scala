package models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Point(id: Option[Long], boardId: String, date: DateTime, scope: Int, progress: Int, finished: Int)

object Point {
  implicit val reads = Json.reads[Point]
  implicit val writes = Json.writes[Point]
}