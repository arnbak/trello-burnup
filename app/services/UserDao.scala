package services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.collection.mutable
import scala.concurrent.Future

import UserDAOImpl._

trait UserDAO {

  def find(loginInfo: LoginInfo): Future[Option[User]]

  def find(userID: UUID): Future[Option[User]]

  def save(user: User): Future[User]
}

/**
 * Give access to the user object.
 */
class UserDAOImpl extends UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = Future.successful(
    users.find { case (id, user) => user.loginInfo == loginInfo }.map(_._2)
  )

  /*
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID) = Future.successful(users.get(userID))

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    users += (user.userID -> user)
    Future.successful(user)
  }
}

/**
 * The companion object.
 */
object UserDAOImpl {

  /**
   * The list of users.
   */
  val users: mutable.HashMap[UUID, User] = mutable.HashMap()
}

//package services
//
//import java.security.MessageDigest
//import java.util.UUID
//
//import com.google.inject.{ Singleton, ImplementedBy, Inject }
//import com.mohiva.play.silhouette.api.LoginInfo
//import models.User
//import play.api.db.Database
//import anorm._
//import anorm.SqlParser._
//
//import scala.concurrent.Future
//
//@ImplementedBy(classOf[DBUserDao])
//trait UserDAO {
//  def findAll: List[User]
//  def findByEmail(email: String): Option[User]
//  def create(email: String, username: Option[String], password: String): Option[User]
//  def addKey(email: String, key: String): Option[User]
//  def addToken(email: String, token: String): Option[User]
//  def validateUser(email: String, password: String): Option[User]
//
//  //Silhouette
//  def find(loginInfo: LoginInfo): Option[User]
//  //  def find(userId:UUID):Future[Option[User]]
//  //  def save(user:User):Future[User]
//  //  def confirm(loginInfo:LoginInfo):Future[User]
//  //  def link(user:User, profile:Profile):Future[User]
//  //  def update(profile:Profile):Future[User]
//}
//
//@Singleton
//class DBUserDao @Inject() (db: Database) extends UserDAO {
//
//  private val userParser = {
//    get[Long]("id") ~
//      get[String]("email") ~
//      get[Option[String]]("username") ~
//      get[Option[String]]("appkey") ~
//      get[Option[String]]("apptoken") map {
//        case id ~ email ~ username ~ appkey ~ apptoken => User(id, None, None, email, username, appkey, apptoken, None)
//      }
//  }
//
//  def findAll = {
//    db.withConnection { implicit c =>
//      SQL("SELECT id,email,username,appkey,apptoken FROM public.users").as(userParser.*)
//    }
//  }
//
//  def findByEmail(email: String) = {
//    db.withConnection { implicit c =>
//      SQL"""SELECT id,email,username,appkey,apptoken FROM public.users WHERE email=$email""".as(userParser.singleOpt)
//    }
//  }
//
//  def create(email: String, username: Option[String], password: String) = {
//    val pw = getShaPassword(password)
//
//    db.withConnection { implicit c =>
//      SQL"""INSERT INTO public.users (email, username, password) VALUES ($email, $username,$pw)""".executeInsert()
//    } match {
//      case Some(newId) => findByEmail(email)
//      case _ => None
//    }
//  }
//
//  private def getShaPassword(password: String): Array[Byte] = {
//    MessageDigest.getInstance("SHA-512").digest(password.getBytes("UTF-8"))
//  }
//
//  def addKey(email: String, key: String) = {
//    db.withConnection { implicit c =>
//      SQL"""UPDATE public.users SET appkey = $key WHERE email=$email""".executeUpdate()
//    } match {
//      case updated: Int => if (updated == 1) findByEmail(email) else None
//    }
//  }
//
//  def addToken(email: String, token: String) = {
//    db.withConnection { implicit c =>
//      SQL"""UPDATE public.users SET apptoken = $token WHERE email=$email""".executeUpdate()
//    } match {
//      case updated: Int => if (updated == 1) findByEmail(email) else None
//    }
//  }
//
//  def validateUser(email: String, password: String) = {
//    val pw = getShaPassword(password)
//
//    db.withConnection { implicit c =>
//      SQL"""SELECT id,email,username,appkey,apptoken FROM public.users WHERE email=$email AND password=$pw""".as(userParser.singleOpt)
//    }
//  }
//
//  def find(loginInfo: LoginInfo) = {
//    findByEmail(loginInfo.providerID)
//  }
//
//}