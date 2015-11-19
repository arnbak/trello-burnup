package controllers

import javax.inject.{ Inject, Singleton }

import forms.LoginForm
import models.PageInfo
import play.api.Environment
import play.api.i18n.{ MessagesApi, I18nSupport }

import play.api.mvc._
import services.UserService

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class LoginController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def loginPage = Action { implicit request =>
    Ok(views.html.login(PageInfo("Sign in", request.uri), LoginForm.form))
  }

  def create = Action.async { implicit request =>
    LoginForm.form.bindFromRequest.fold(
      error => Future.successful(BadRequest(views.html.create(PageInfo("Create a user", request.uri), error))),
      success => {
        userService.findByEmail(Some(success.email)).flatMap { userOpt =>
          userOpt.map { user =>
            Future.successful(Redirect(routes.LoginController.createPage()).flashing("error" -> "That email address is already in use in this system!"))
          } getOrElse {
            userService.createUser(success.email, success.password).map { _ =>
              Redirect(routes.LoginController.loginPage()).flashing("success" -> "The user have been created, now go ahead and sign in with it!")
            }
          }
        }
      }
    )
  }

  def createPage = Action { implicit request =>
    Ok(views.html.create(PageInfo("Create User", request.uri), LoginForm.form))
  }

  def login = Action.async { implicit request =>
    LoginForm.form.bindFromRequest.fold(
      error => Future.successful(BadRequest(views.html.login(PageInfo("Sign in", request.uri), error))),
      success =>
        userService.validateUser(success.email, success.password).map { userOpt =>
          userOpt.map { u =>
            Redirect(routes.Application.dashboard()).flashing("success" -> "Welcome honored user.").withSession("email" -> u.email)
          } getOrElse {
            Redirect(routes.LoginController.loginPage()).flashing("error" -> "We could not sign you in, the user/password combination doesn't look right! Give it another try!")
          }
        }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.LoginController.loginPage()).withNewSession.flashing("success" -> "You are now signed out, thank you for using this application! Please come again!")
  }

}