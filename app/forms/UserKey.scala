package forms

import play.api.data.Form
import play.api.data.Forms._

object UserKey {

  case class Key(key: String)

  val form = Form(
    mapping(
      "key" -> nonEmptyText()
    )(Key.apply)(Key.unapply)
  )
}
