package services

import java.security.MessageDigest

import com.google.inject.{ Singleton, ImplementedBy, Inject }
import models.User
import play.api.db.Database
import anorm._
import anorm.SqlParser._

@ImplementedBy(classOf[DBUserDao])
trait UserDAO {
  def findAll: List[User]
  def findByEmail(email: String): Option[User]
  def create(email: String, username: Option[String], password: String): Option[User]
  def addKey(email: String, key: String): Option[User]
  def addToken(email: String, token: String): Option[User]
  def validateUser(email: String, password: String): Option[User]
}

@Singleton
class DBUserDao @Inject() (db: Database) extends UserDAO {

  private val userParser = {
    get[Long]("id") ~
      get[String]("email") ~
      get[Option[String]]("username") ~
      get[Option[String]]("appkey") ~
      get[Option[String]]("apptoken") map {
        case id ~ email ~ username ~ appkey ~ apptoken => User(id, None, None, email, username, appkey, apptoken, None)
      }
  }

  def findAll = {
    db.withConnection { implicit c =>
      SQL("SELECT id,email,username,appkey,apptoken FROM public.users").as(userParser.*)
    }
  }

  def findByEmail(email: String) = {
    db.withConnection { implicit c =>
      SQL"""SELECT id,email,username,appkey,apptoken FROM public.users WHERE email=$email""".as(userParser.singleOpt)
    }
  }

  def create(email: String, username: Option[String], password: String) = {
    val pw = getShaPassword(password)

    db.withConnection { implicit c =>
      SQL"""INSERT INTO public.users (email, username, password) VALUES ($email, $username,$pw)""".executeInsert()
    } match {
      case Some(newId) => findByEmail(email)
      case _ => None
    }
  }

  private def getShaPassword(password: String): Array[Byte] = {
    MessageDigest.getInstance("SHA-512").digest(password.getBytes("UTF-8"))
  }

  def addKey(email: String, key: String) = {
    db.withConnection { implicit c =>
      SQL"""UPDATE public.users SET appkey = $key WHERE email=$email""".executeUpdate()
    } match {
      case updated: Int => if (updated == 1) findByEmail(email) else None
    }
  }

  def addToken(email: String, token: String) = {
    db.withConnection { implicit c =>
      SQL"""UPDATE public.users SET apptoken = $token WHERE email=$email""".executeUpdate()
    } match {
      case updated: Int => if (updated == 1) findByEmail(email) else None
    }
  }

  def validateUser(email: String, password: String) = {
    val pw = getShaPassword(password)

    db.withConnection { implicit c =>
      SQL"""SELECT id,email,username,appkey,apptoken FROM public.users WHERE email=$email AND password=$pw""".as(userParser.singleOpt)
    }
  }

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
}