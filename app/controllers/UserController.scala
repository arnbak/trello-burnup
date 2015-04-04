package controllers

import models.Users
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.db.slick._


object UserController extends Controller with Secured {



  case class UserKey(key: String)

  val userKeyForm = Form(
    mapping(
      "key" -> nonEmptyText()
    )(UserKey.apply)(UserKey.unapply)
  )

  def keyForm = IsAuthenticated { user => implicit request =>
    Ok(views.html.fragments.keyform(userKeyForm, Some(user)))
  }

  def saveKey = IsAuthenticated { user => implicit request =>
    userKeyForm.bindFromRequest.fold(
      error => BadRequest(views.html.fragments.keyform(error, Some(user))),
      success => {
        Users.addKey(user.email, success.key).map { u =>
          Redirect(routes.Application.dashboard()).flashing("success" -> "Nøglen er gemt! Du skal nu genere et token!")
        } getOrElse {
          Redirect(routes.UserController.saveKey()).flashing("error" -> "Nøglen kunne ikke gemmes")
        }
      }
    )
  }

  case class UserToken(token: String)

  val userTokenForm = Form(
    mapping(
      "token" -> nonEmptyText()
    )(UserToken.apply)(UserToken.unapply)
  )

  def tokenForm = IsAuthenticated { user => implicit request =>
    Ok(views.html.fragments.tokenform(userTokenForm, Some(user)))
  }


  def saveToken = IsAuthenticated { user => implicit request =>
    userTokenForm.bindFromRequest.fold(
      error => BadRequest(views.html.fragments.tokenform(error, Some(user))),
      success => {
          Users.addToken(user.email, success.token).map { u =>
            Redirect(routes.Application.dashboard()).flashing("success" -> "Token er gemt!")
          } getOrElse {
            Redirect(routes.Application.dashboard()).flashing("error" -> "Token er ikke gemt!")
          }

      }
    )
  }

  def profile = IsAuthenticated { user =>  implicit request =>
    Ok(views.html.profile.profile("Profil", Some(user)))
  }





}