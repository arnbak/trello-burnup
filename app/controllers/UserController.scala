package controllers

import models.{PageInfo, Users}
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
    Ok(views.html.fragments.keyform(PageInfo("Add trello key", request.uri), userKeyForm, Some(user)))
  }

  def saveKey = IsAuthenticated { user => implicit request =>
    userKeyForm.bindFromRequest.fold(
      error => BadRequest(views.html.fragments.keyform(PageInfo("Add trello key", request.uri), error, Some(user))),
      success => {
        Users.addKey(user.email, success.key).map { u =>
          Redirect(routes.Application.dashboard()).flashing("success" -> "The key is saved, its time for you to generate the token!")
        } getOrElse {
          Redirect(routes.UserController.saveKey()).flashing("error" -> "We could not save that key!")
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
    Ok(views.html.fragments.tokenform(PageInfo("Enter token", request.uri), userTokenForm, Some(user)))
  }


  def saveToken = IsAuthenticated { user => implicit request =>
    userTokenForm.bindFromRequest.fold(
      error => BadRequest(views.html.fragments.tokenform(PageInfo("Enter token", request.uri), error, Some(user))),
      success => {
          Users.addToken(user.email, success.token).map { u =>
            Redirect(routes.Application.dashboard()).flashing("success" -> "The token is saved!")
          } getOrElse {
            Redirect(routes.Application.dashboard()).flashing("error" -> "We could not save the token!")
          }

      }
    )
  }

  def profile = IsAuthenticated { user =>  implicit request =>
    Ok(views.html.profile.profile(PageInfo("Profile", request.uri), Some(user)))
  }





}