package services

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[User]
}

//package services
//
//import java.util.UUID
//import javax.inject.Inject
//
//import com.mohiva.play.silhouette.api.LoginInfo
//import com.mohiva.play.silhouette.api.services.IdentityService
//import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
//import models.User
//
//import scala.concurrent.Future
//
//trait UserService extends IdentityService[User] {
//  def save(user: User): Future[User]
//  def save(profile: CommonSocialProfile): Future[User]
//}
//
//class UserServiceImpl @Inject() (userDAO: UserDAO) extends UserService {
//
//  def retrive(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)
//
//  def save(profile: CommonSocialProfile) = {
//    userDAO.find(profile.loginInfo).flatMap {
//      case Some(user) => // Update user with profile
//        userDAO.save(user.copy(
//          firstName = profile.firstName,
//          lastName = profile.lastName,
//          fullName = profile.fullName,
//          email = profile.email,
//          avatarURL = profile.avatarURL
//        ))
//      case None => // Insert a new user
//        userDAO.save(User(
//          userID = UUID.randomUUID(),
//          loginInfo = profile.loginInfo,
//          firstName = profile.firstName,
//          lastName = profile.lastName,
//          fullName = profile.fullName,
//          email = profile.email,
//          avatarURL = profile.avatarURL
//        ))
//    }
//  }
//}
//
////package services
////
////import com.google.inject.{ ImplementedBy, Inject, Singleton }
////import com.mohiva.play.silhouette.api.LoginInfo
////import com.mohiva.play.silhouette.api.services.IdentityService
////import models.User
////
////import scala.concurrent.{ ExecutionContext, Future }
////
////@ImplementedBy(classOf[UserService])
////trait IUserService {
////  def allUsers: Future[List[User]]
////  def findByEmail(email: Option[String]): Future[Option[User]]
////  def validateUser(email: String, password: String): Future[Option[User]]
////  def createUser(email: String, password: String): Future[Option[User]]
////  def addKey(email: String, key: String): Future[Option[User]]
////  def addToken(email: String, token: String): Future[Option[User]]
////}
////
////@Singleton
////class UserService @Inject() (userDao: UserDAO)(implicit ec: ExecutionContext) extends IdentityService[User] with IUserService {
////
////  def allUsers = Future {
////    userDao.findAll
////  }
////
////  def findByEmail(email: Option[String]): Future[Option[User]] = Future {
////    email match {
////      case Some(emailStr) => userDao.findByEmail(emailStr)
////      case _ => None
////    }
////  }
////
////  def validateUser(email: String, password: String) = Future {
////    userDao.validateUser(email, password)
////  }
////
////  def createUser(email: String, password: String) = Future {
////    userDao.create(email, None, password)
////  }
////
////  def addKey(email: String, key: String) = Future {
////    userDao.addKey(email, key)
////  }
////
////  def addToken(email: String, token: String) = Future {
////    userDao.addToken(email, token)
////  }
////
////  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = Future {
////    userDao.find(loginInfo)
////  }
////
////}