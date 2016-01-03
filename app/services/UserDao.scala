package services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import play.api.Logger
import play.api.db.Database

import scala.concurrent.Future

import anorm.SqlParser._
import anorm._

trait UserDAO {

  def allUsers: Future[List[User]]

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

  def allUsers: Future[List[User]] = Future.successful {
    db.withConnection { implicit c =>
      SQL"""SELECT * FROM public.users""".as(userParser.*)
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

      val user = db.withConnection { implicit c =>
        SQL"""SELECT * FROM public.users WHERE providerid=${loginInfo.providerID} AND providerkey=${loginInfo.providerKey}""".as(userParser.singleOpt)
      }

      user
    }
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