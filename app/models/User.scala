package models

import java.util.UUID
import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class User(
  id: Long,
  userID: Option[UUID],
  loginInfo: Option[LoginInfo],
  email: String,
  username: Option[String],
  key: Option[String],
  token: Option[String],
  avatarURL: Option[String]) extends Identity
