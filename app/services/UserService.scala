package services

import com.google.inject.{ ImplementedBy, Inject, Singleton }
import models.User

import scala.concurrent.{ ExecutionContext, Future }

@ImplementedBy(classOf[UserServiceImpl])
trait UserService {
  def allUsers: Future[List[User]]
  def findByEmail(email: Option[String]): Future[Option[User]]
  def validateUser(email: String, password: String): Future[Option[User]]
  def createUser(email: String, password: String): Future[Option[User]]
  def addKey(email: String, key: String): Future[Option[User]]
  def addToken(email: String, token: String): Future[Option[User]]
}

@Singleton
class UserServiceImpl @Inject() (userDao: UserDAO)(implicit ec: ExecutionContext) extends UserService {

  def allUsers = Future {
    userDao.findAll
  }

  def findByEmail(email: Option[String]): Future[Option[User]] = Future {
    email match {
      case Some(emailStr) => userDao.findByEmail(emailStr)
      case _ => None
    }
  }

  def validateUser(email: String, password: String) = Future {
    userDao.validateUser(email, password)
  }

  def createUser(email: String, password: String) = Future {
    userDao.create(email, None, password)
  }

  def addKey(email: String, key: String) = Future {
    userDao.addKey(email, key)
  }

  def addToken(email: String, token: String) = Future {
    userDao.addToken(email, token)
  }
}