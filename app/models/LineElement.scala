package models

import play.api.libs.json._

case class LineElement(x: Long, y: Double)

//object LineElement {
//  implicit val writes = Json.writes[LineElement]
//  implicit val reads = Json.reads[LineElement]
//}

object LineElement {

  implicit object LineElementFormat extends Format[LineElement] {

    def reads(json: JsValue): JsSuccess[LineElement] = {

      val arr = json.as[JsArray]

      JsSuccess(LineElement(arr(0).as[Long], arr(1).as[Double]))
    }

    def writes(l: LineElement): JsValue = {
      JsArray(Seq(JsNumber(l.x), JsNumber(l.y)))
    }
  }
}