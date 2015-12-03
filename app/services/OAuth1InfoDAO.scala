package services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import play.api.db.Database

import anorm.SqlParser._
import anorm._

import play.api.libs.concurrent.Execution.Implicits._

import scala.collection.mutable
import scala.concurrent.Future

class OAuth1InfoDAO @Inject() (db: Database) extends DelegableAuthInfoDAO[OAuth1Info] {

  private val oAuth1InfoParser = {
    get[String]("token") ~
      get[String]("secret") map {
        case token ~ secret => OAuth1Info(token, secret)
      }
  }

  /**
   * Finds the auth info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
   */
  def find(loginInfo: LoginInfo): Future[Option[OAuth1Info]] = {
    Future.successful {
      db.withConnection { implicit c =>
        SQL"SELECT token,secret FROM public.oauth1info WHERE providerid=${loginInfo.providerID} AND providerkey=${loginInfo.providerKey}".as(oAuth1InfoParser.singleOpt)
      }
    }
  }

  /**
   * Adds new auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be added.
   * @param authInfo The auth info to add.
   * @return The added auth info.
   */
  def add(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {

    Future.successful {
      db.withConnection { implicit c =>
        SQL"""INSERT INTO public.oauth1info (providerid,providerkey,token,secret) VALUES (${loginInfo.providerID},${loginInfo.providerKey},${authInfo.token},${authInfo.secret})""".executeInsert()
      }

      authInfo
    }
  }

  /**
   * Updates the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be updated.
   * @param authInfo The auth info to update.
   * @return The updated auth info.
   */
  def update(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {

    Future.successful {
      db.withConnection { implicit c =>
        SQL"""
          UPDATE public.oauth1info
          SET token=${authInfo.token},secret=${authInfo.secret}
          WHERE providerid=${loginInfo.providerID} AND providerkey=${loginInfo.providerKey}
        """
      }
      authInfo
    }
  }

  /**
   * Saves the auth info for the given login info.
   *
   * This method either adds the auth info if it doesn't exists or it updates the auth info
   * if it already exists.
   *
   * @param loginInfo The login info for which the auth info should be saved.
   * @param authInfo The auth info to save.
   * @return The saved auth info.
   */
  def save(loginInfo: LoginInfo, authInfo: OAuth1Info): Future[OAuth1Info] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  /**
   * Removes the auth info for the given login info.
   *
   * @param loginInfo The login info for which the auth info should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(loginInfo: LoginInfo): Future[Unit] = {

    Future.successful {
      db.withConnection { implicit c =>
        SQL"""DELETE FROM public.oauth1info WHERE providerid=${loginInfo.providerID} AND providerkey=${loginInfo.providerKey}"""
      }
    }

  }
}

/**
 * The companion object.
 */
object OAuth1InfoDAO {

  /**
   * The data store for the OAuth1 info.
   */
  var data: mutable.HashMap[LoginInfo, OAuth1Info] = mutable.HashMap()
}