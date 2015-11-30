/**
 * Original work: SecureSocial (https://github.com/jaliss/securesocial)
 * Copyright 2013 Brian Porter (poornerd at gmail dot com) - twitter: @poornerd
 *
 * Derivative work: Silhouette (https://github.com/mohiva/play-silhouette)
 * Modifications Copyright 2015 Mohiva Organisation (license at mohiva dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package modules

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers._
import modules.TrelloProvider._
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.Future

/**
 * Base Tello OAuth1 Provider.
 *
 * @see https://dev.xing.com/docs/get/users/me
 * @see https://dev.xing.com/docs/error_responses
 */
//TODO fix paths to trello json responses
trait BaseTrelloProvider extends OAuth1Provider {

  /**
   * The content type to parse a profile from.
   */
  override type Content = JsValue

  /**
   * The provider ID.
   */
  override val id = ID

  /**
   * Defines the URLs that are needed to retrieve the profile data.
   */
  override protected val urls = Map("api" -> API)

  /**
   * Builds the social profile.
   *
   * @param authInfo The auth info received from the provider.
   * @return On success the build social profile, otherwise a failure.
   */
  override protected def buildProfile(authInfo: OAuth1Info): Future[Profile] = {
    httpLayer.url(urls("api")).sign(service.sign(authInfo)).get().flatMap { response =>

      Logger.info("Response " + response.statusText)
      val json = response.json
      (json \ "error_name").asOpt[String] match {
        case Some(error) =>
          val message = (json \ "message").asOpt[String]

          Future.failed(new ProfileRetrievalException(SpecifiedProfileError.format(id, error, message.getOrElse(""))))
        case _ => profileParser.parse(json)
      }
    }
  }
}

/**
 * The profile parser for the common social profile.
 */
class TrelloProfileParser extends SocialProfileParser[JsValue, CommonSocialProfile] {

  /**
   * Parses the social profile.
   *
   * @param json The content returned from the provider.
   * @return The social profile from given result.
   */
  override def parse(json: JsValue) = Future.successful {

    Logger.info("Json " + json)

    //val users = (json \ "users").as[Seq[JsObject]].head
    val userID = (json \ "id").as[String]
    val firstName = (json \ "first_name").asOpt[String]
    val lastName = (json \ "last_name").asOpt[String]
    val fullName = (json \ "fullName").asOpt[String]
    val avatarURL = (json \ "url").asOpt[String]
    val email = (json \ "email").asOpt[String]

    CommonSocialProfile(
      loginInfo = LoginInfo(ID, userID),
      firstName = firstName,
      lastName = lastName,
      fullName = fullName,
      avatarURL = avatarURL,
      email = email)
  }
}

/**
 * The Xing OAuth1 Provider.
 *
 * @param httpLayer The HTTP layer implementation.
 * @param service The OAuth1 service implementation.
 * @param tokenSecretProvider The OAuth1 token secret provider implementation.
 * @param settings The OAuth1 provider settings.
 */
class TrelloProvider(
  protected val httpLayer: HTTPLayer,
  protected val service: OAuth1Service,
  protected val tokenSecretProvider: OAuth1TokenSecretProvider,
  val settings: OAuth1Settings)
    extends BaseTrelloProvider with CommonSocialProfileBuilder {

  /**
   * The type of this class.
   */
  override type Self = TrelloProvider

  /**
   * The profile parser implementation.
   */
  override val profileParser = new TrelloProfileParser

  /**
   * Gets a provider initialized with a new settings object.
   *
   * @param f A function which gets the settings passed and returns different settings.
   * @return An instance of the provider initialized with new settings.
   */
  override def withSettings(f: (Settings) => Settings) = {
    new TrelloProvider(httpLayer, service, tokenSecretProvider, f(settings))
  }
}

/**
 * The companion object.
 */
object TrelloProvider {

  /**
   * The error messages.
   */
  val SpecifiedProfileError = "[Silhouette][%s] error retrieving profile information. Error name: %s, message: %s"

  /**
   * The Trello constants.
   */
  val ID = "trello"
  val API = "https://api.trello.com/1/members/me?fields=username,email,fullName,url&boards=all&board_fields=name&organizations=all&organization_fields=displayName"
}
