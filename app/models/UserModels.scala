//package models
//
//import java.security.MessageDigest
//import java.util.{UUID, Date}
//import java.sql.{ Date => SqlDate }
//import play.Logger
//import scala.slick.driver.PostgresDriver.simple._
//
////case class User(id: Option[Long], email: String, username: Option[String], key: Option[String], token: Option[String])
//
//class Users(tag: Tag) extends Table[(Option[Long], String, Option[String], Array[Byte], Option[String], Option[String])](tag, "USERS") {
//  def id = column[Option[Long]]("ID", O.AutoInc)
//  def email = column[String]("EMAIL", O.PrimaryKey,O.NotNull)
//  def username = column[Option[String]]("USERNAME")
//  def password = column[Array[Byte]]("PASSWORD", O.NotNull)
//  def appKey = column[Option[String]]("APPKEY")
//  def appToken = column[Option[String]]("APPTOKEN")
//
//  def * = (id, email, username, password, appKey, appToken)
//}
//
////case class AppToken(id: Option[Long], email: String,token: String, added: Date)
////
////class AppTokens(tag: Tag) extends Table[(Long, String, String, Date)](tag, "APPTOKENS") {
////
////  implicit val dateColumnType = MappedColumnType.base[Date, Long](d => d.getTime, d => new Date(d))
////
////  def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
////  def email = column[String]("EMAIL", O.NotNull)
////  def token = column[String]("TOKEN", O.NotNull)
////  def added = column[Date]("ADDED", O.NotNull)
////
////  def * = (id, email, token, added)
////}
//
//
//
//object Users {
//
//  val users = TableQuery[Users]
//
//
//  private def getShaPassword(password: String): Array[Byte] = {
//    MessageDigest.getInstance("SHA-512").digest(password.getBytes("UTF-8"))
//  }
//
//  def create(email: String, username: Option[String], password: String)(implicit s: Session): Option[User] = {
//    val result = users.map(u => (u.email, u.username, u.password))
//      .insert(email, username, getShaPassword(password))
//
//    if(result != 0) findByEmail(email)
//    else None
//  }
//
//  def findByEmail(email: String)(implicit s: Session): Option[User] = {
//    users.filter(u => u.email === email).firstOption.map(
//      u => User(u._1, u._2, u._3, u._5, u._6)
//    )
//
//
//  }
//
//  def validUser(email: String, password: String)(implicit s: Session): Option[User] = {
//    users.filter(u => u.email === email && u.password === getShaPassword(password)).firstOption.map {
//      u => User(u._1, u._2, u._3, u._5, u._6)
//    }
//  }
//
//  def listUsers(implicit s: Session): List[User] = {
//    users.sortBy(u=>u.email.desc).list.map(u => User(u._1, u._2, u._3, u._5, u._6))
//  }
//
//
//
//
//  def addKey(email: String, key: String)(implicit s: Session): Option[User] = {
//
//    val updateResult = users.filter(u => u.email === email)
//      .map(u => u.appKey)
//      .update(Some(key))
//
//    Logger.info("Updated " + updateResult)
//
//    findByEmail(email)
//  }
//
//  def addToken(email: String, token: String)(implicit s: Session): Option[User] = {
//    users.filter(u => u.email === email)
//      .map(u => u.appToken)
//      .update(Some(token))
//
//    findByEmail(email)
//  }
//
//}