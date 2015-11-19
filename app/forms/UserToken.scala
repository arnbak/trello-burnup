package forms

import play.api.data.Form
import play.api.data.Forms._

object UserToken {
  case class UserToken(token: String)

  val form = Form(
    mapping(
      "token" -> nonEmptyText()
    )(UserToken.apply)(UserToken.unapply)
  )

}

