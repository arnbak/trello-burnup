package forms

import play.api.data.Form
import play.api.data.Forms._

object LoginForm {
  val form = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply)
  )
  case class Credentials(email: String, password: String)
}

