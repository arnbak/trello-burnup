package controllers

import models.{PageInfo, Users}
import play.Logger
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._

import play.api.mvc._

object LoginController extends Controller with Secured {

  case class LoginForm(email: String, password: String)

  val loginForm = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )

  def loginPage = Action { implicit request =>
    Ok(views.html.login(PageInfo("Sign in", request.uri), loginForm))
  }

  def create = DBAction { implicit request =>
    loginForm.bindFromRequest.fold(
      error => BadRequest(views.html.create(PageInfo("Create a user", request.uri), error)),
      success =>
        Users.findByEmail(success.email) match {
          case Some(u) => Redirect(routes.LoginController.createPage()).flashing("error" -> "That email address is already in use in this system!")
          case None =>
            Users.create(success.email, None, success.password).map { user =>
              Redirect(routes.LoginController.loginPage()).flashing("success" -> "The user have been created, welcome!")
            } getOrElse {
              Redirect(routes.LoginController.createPage()).flashing("error" -> "We had no luck creating the user")
            }

        }
    )
  }

  def createPage = Action { implicit request =>
    Ok(views.html.create(PageInfo("Create User", request.uri), loginForm))
  }

  def login = DBAction { implicit request =>
    loginForm.bindFromRequest.fold(
      error => BadRequest(views.html.login(PageInfo("Sign in", request.uri), error)),
      success =>
        Users.validUser(success.email, success.password).map { user =>
          Redirect(routes.Application.dashboard()).flashing("success" -> "").withSession("email" -> user.email)
        } getOrElse {
          Redirect(routes.LoginController.loginPage()).flashing("error" -> "We could not sign you in ")
        }
    )
  }

  def logout = IsAuthenticated { user => implicit request =>
    Redirect(routes.LoginController.loginPage()).withNewSession.flashing("success" -> "Welcome, you are signed in!")
  }

}