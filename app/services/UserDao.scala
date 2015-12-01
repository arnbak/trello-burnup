package services

import java.util.UUID
import javax.inject.Inject

import akka.event.Logging
import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.Logger
import play.api.db.Database

import scala.concurrent.Future

import anorm.SqlParser._
import anorm._

import scala.util.{ Failure, Try }

trait UserDAO {

  def find(loginInfo: LoginInfo): Future[Option[User]]

  def find(userID: UUID): Future[Option[User]]

  def save(user: User): Future[User]

  def update(user: User): Future[User]
}

/**
 * Give access to the user object.
 */
class UserDAOImpl @Inject() (db: Database) extends UserDAO {

  case class UserHelper(id: String, providerid: String, providerkey: String, firstName: Option[String], lastName: Option[String], fullName: Option[String], email: Option[String], avatarUrl: Option[String])

  private val userParser = {
    get[UUID]("userid") ~
      get[String]("providerid") ~
      get[String]("providerkey") ~
      get[Option[String]]("firstname") ~
      get[Option[String]]("lastname") ~
      get[Option[String]]("fullname") ~
      get[Option[String]]("email") ~
      get[Option[String]]("avatarurl") map {
        case uId ~ providerid ~ providerkey ~ firstname ~ lastname ~ fullname ~ email ~ avatarurl => User(uId, LoginInfo(providerid, providerkey), firstname, lastname, fullname, email, avatarurl)
      }
  }

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {

    Future.successful {
      Logger.info(s"trying to find user $loginInfo")

      val user = db.withConnection { implicit c =>
        SQL"""SELECT * FROM public.users WHERE providerid=${loginInfo.providerID} AND providerkey=${loginInfo.providerKey}""".as(userParser.singleOpt)
      }

      Logger.info(s"Got user $user")

      user
    }
    //    Future.successful(
    //      users.find {
    //        case (id, user) => user.loginInfo == loginInfo
    //      }.map(_._2)
    //    )
  }

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID) = {

    Future.successful {

      Logger.info(s"Trying to find user $userID")

      val user = db.withConnection { implicit c =>
        SQL"""SELECT * FROM public.users WHERE userid=$userID""".as(userParser.singleOpt)
      }

      user
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    try {

      db.withConnection { implicit c =>
        SQL"""INSERT INTO public.users (userid, providerid, providerkey, firstname, lastname, fullname, email, avatarurl) VALUES (${user.userID},${user.loginInfo.providerID},${user.loginInfo.providerKey},${user.firstName}, ${user.lastName}, ${user.fullName}, ${user.email}, ${user.avatarURL})""".executeInsert()
      }

    } catch {
      case e: Throwable => Logger.info(s"Exception ${e.getStackTrace}", e)
      //      case e: AnormException =>
      //        e.printStackTrace()
      //        Logger.error(s"Error ${e.getMessage()} - ${e.printStackTrace()}", e.getCause)
    }

    Logger.info(s"User created $user")

    Future.successful(user)
  }

  def update(user: User) = {
    Future.successful {

      db.withConnection { implicit c =>
        SQL"""UPDATE public.users
             SET firstname=${user.firstName},lastname=${user.lastName},fullname=${user.fullName},email=${user.email},avatarurl=${user.avatarURL}
             WHERE userid=${user.userID}
           """.executeUpdate()
      }

      Logger.info(s"Updated user $user")

      user
    }
  }
}

///**
// * The companion object.
// */
//object UserDAOImpl {
//
//  /**
//   * The list of users.
//   */
//  val users: mutable.HashMap[UUID, User] = mutable.HashMap()
//}

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