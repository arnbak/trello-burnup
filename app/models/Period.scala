package models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class Period(id: Option[Long], boardId: String, startDate: DateTime, endDate: DateTime, periodInDayes: Option[Int] = Some(0))

object Period {
  implicit val reads = Json.reads[Period]
  implicit val writes = Json.writes[Period]
}