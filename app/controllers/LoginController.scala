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
    Ok(views.html.login(PageInfo("Log ind", request.uri), loginForm))
  }

  def create = DBAction { implicit request =>
    loginForm.bindFromRequest.fold(
      error => BadRequest(views.html.create("Opret", error)),
      success =>
        Users.findByEmail(success.email) match {
          case Some(u) => Redirect(routes.LoginController.createPage()).flashing("error" -> "Den valgte email addresse findes allerede.")
          case None =>
            Users.create(success.email, None, success.password).map { user =>
              Redirect(routes.LoginController.loginPage()).flashing("success" -> "Brugeren er oprettet og du kan nu logge ind")
            } getOrElse {
              Redirect(routes.LoginController.createPage()).flashing("error" -> "Kunne ikke oprettes !")
            }

        }
    )
  }

  def createPage = Action { implicit request =>
    Ok(views.html.create("Opret", loginForm))
  }

  def login = DBAction { implicit request =>
    loginForm.bindFromRequest.fold(
      error => BadRequest(views.html.login(PageInfo("Log på", request.uri), error)),
      success =>
        Users.validUser(success.email, success.password).map { user =>
          Redirect(routes.Application.dashboard()).flashing("success" -> "").withSession("email" -> user.email)
        } getOrElse {
          Redirect(routes.LoginController.loginPage()).flashing("error" -> "Vi kunne logge dig på via Trello")
        }
    )
  }

  def logout = IsAuthenticated { user => implicit request =>
    Redirect(routes.LoginController.loginPage()).withNewSession.flashing("success" -> "Du er nu logget ud!.")
  }

}