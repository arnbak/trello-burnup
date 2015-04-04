import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.Logger
import play.api.http.Status

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/gibberishurl")) must beNone
    }

    "trying to access / should redirect to login" in new WithApplication {
      val login = route(FakeRequest(GET, "/")).get
      status(login) must equalTo(SEE_OTHER)
    }

    "render the login page" in new WithApplication {
      val login = route(FakeRequest(GET, "/login")).get

      status(login) must equalTo(OK)

      Logger.info("login " + login)

      print("Login " + login)

      contentType(login) must beSome.which(_ == "text/html")
      contentAsString(login) must contain ("Log på")
    }

  }
}
