package models

import play.api.libs.json._

case class LineElement(x: Long, y: Double)

object LineElement {
  implicit val writes = Json.writes[LineElement]
  implicit val reads = Json.reads[LineElement]
}