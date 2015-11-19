package models

import play.api.libs.json._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Member(id: String,
  fullName: String,
  initials: String,
  memberType: String,
  products: List[Int],
  url: String,
  username: String,
  gravatarHash: String,
  idBoards: List[String],
  idOrganizations: Option[List[String]],
  idBoardsPinned: Option[List[String]],
  boards: List[Board])

object Member {

  implicit val memberReads: Reads[Member] = (
    (__ \ "id").read[String] and
    (__ \ "fullName").read[String] and
    (__ \ "initials").read[String] and
    (__ \ "memberType").read[String] and
    (__ \ "products").read[List[Int]] and
    (__ \ "url").read[String] and
    (__ \ "username").read[String] and
    (__ \ "gravatarHash").read[String] and
    (__ \ "idBoards").read[List[String]] and
    (__ \ "idOrganizations").readNullable[List[String]] and
    (__ \ "idBoardsPinned").readNullable[List[String]] and
    (__ \ "boards").read[List[Board]]
  )(Member.apply _)
}

case class Board(name: String,
  closed: Boolean,
  idOrganization: Option[String],
  pinned: Option[String],
  id: String)

object Board {
  implicit val boardReads: Reads[Board] = (
    (__ \ "name").read[String] and
    (__ \ "closed").read[Boolean] and
    (__ \ "idOrganization").readNullable[String] and
    (__ \ "pinned").readNullable[String] and
    (__ \ "id").read[String]
  )(Board.apply _)
}

case class Organization()

case class Card(
  id: String,
  closed: Boolean,
  dateLastActivity: String,
  desc: String,
  name: String,
  idBoard: String,
  idList: String,
  idShort: Int,
  idLabels: List[String],
  labels: List[Label],
  shortUrl: String,
  url: String)

object Card {
  implicit val cardReads: Reads[Card] = (
    (__ \ "id").read[String] and
    (__ \ "closed").read[Boolean] and
    (__ \ "dateLastActivity").read[String] and
    (__ \ "desc").read[String] and
    (__ \ "name").read[String] and
    (__ \ "idBoard").read[String] and
    (__ \ "idList").read[String] and
    (__ \ "idShort").read[Int] and
    (__ \ "idLabels").read[List[String]] and
    (__ \ "labels").read[List[Label]] and
    (__ \ "shortUrl").read[String] and
    (__ \ "url").read[String]
  )(Card.apply _)
}

case class Label(id: String,
  idBoard: String,
  name: String,
  color: String,
  uses: Int)

object Label {
  implicit val labelReads: Reads[Label] = (
    (__ \ "id").read[String] and
    (__ \ "idBoard").read[String] and
    (__ \ "name").read[String] and
    (__ \ "color").read[String] and
    (__ \ "uses").read[Int]
  )(Label.apply _)
}