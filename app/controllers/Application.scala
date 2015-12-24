package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.{ AccumulateBoard, AccumulateBoardForm, AccumulateBoardList }

import models._
import play.api.Logger
import play.api.i18n.MessagesApi
import play.api.libs.json._
import play.api.libs.json.Json._

import services.{ BoardService, TrelloService }
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class Application @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[User, CookieAuthenticator],
    trelloService: TrelloService,
    boardService: BoardService) extends Silhouette[User, CookieAuthenticator] {

  def dashboard = SecuredAction.async { implicit request =>

    val remoteBoardFuture = trelloService.member(request.identity).flatMap { memberResult =>

      val boards = memberResult.boards.map { board =>
        boardService.findBoardById(board.id).map { dbBoardOpt =>
          dbBoardOpt.map { b =>
            List(b)
          } getOrElse {
            List.empty
          }
        }
      }

      Future.sequence(boards).map { l => l.flatten }
    }

    remoteBoardFuture.map { boardList =>
      Ok(views.html.index(PageInfo("Dashboard", request.uri), Some(request.identity), boardList))
    }
  }

  def profile = SecuredAction.async { implicit request =>

    val remoteBoardFuture = trelloService.member(request.identity).flatMap { memberResult =>

      val list = memberResult.boards.map { board =>
        boardService.findBoardById(board.id).map { dbBoardOpt =>

          dbBoardOpt.map { dbBoard =>
            AccumulateBoardForm(dbBoard.id, dbBoard.name, dbBoard.selected)
          } getOrElse {
            AccumulateBoardForm(board.id, board.name, selected = false)
          }
        }
      }

      Future.sequence(list).map { res =>
        AccumulateBoardList(res)
      }
    }

    remoteBoardFuture.map { list =>
      Ok(views.html.profile.profile(PageInfo("Profile", request.uri), user = Some(request.identity), AccumulateBoard.boardList.fill(list)))
    }
  }

  def board(id: String) = SecuredAction.async { implicit request =>
    trelloService.member(request.identity).map { memberResult =>
      memberResult.boards.find(_.id == id).map { board =>
        Ok(views.html.boards.board(PageInfo("Board Info", request.uri), user = Some(request.identity), board))
      } getOrElse {
        Redirect(routes.Application.dashboard()).flashing("error" -> "That board does not exist")
      }

    }
  }

  def accumulationMark = UserAwareAction.async { implicit request =>

    AccumulateBoard.boardList.bindFromRequest().fold(
      errors => Future.successful {
        Redirect(routes.Application.dashboard())
      },
      boardList => {

        boardService.updateBoard(boardList.boards).map { updatedList =>
          Redirect(routes.Application.dashboard())
        } recoverWith {
          case e: Exception =>
            Logger.error("Error while trying to mark board for accumulation", e)
            Future.successful(Redirect(routes.Application.dashboard()).flashing("error" -> "There were an error marking board for accumulation"))
        }
      }
    )
  }

  def period(boardId: String) = SecuredAction.async { implicit request =>
    boardService.periodByBoardId(boardId).map { period =>
      period.map {
        r => Ok(Json.toJson(r))
      } getOrElse {
        NotFound(Json.obj("message" -> ("no period found for boardid: " + boardId)))
      }
    }
  }

  def series(boardId: String) = SecuredAction.async { implicit request =>

    boardService.periodByBoardId(boardId).map { period =>
      period.map { p =>

        val scopeLine: List[LineElement] = trelloService.createScopeLine(p)
        val finishedLine: List[LineElement] = trelloService.createFinishedLine(p)
        val bestLine: List[LineElement] = trelloService.createBestLine(p, finishedLine)

        Ok(Json.obj("scopeLine" -> scopeLine, "finishedLine" -> finishedLine, "bestLine" -> bestLine))

      }.getOrElse {
        NotFound(Json.obj("message" -> ("no series found for boardid: " + boardId)))
      }
    }

  }

  def accumulateToday = SecuredAction.async { implicit request =>

    val boardInfoFuture = boardService.listBoardsForAccumulation.flatMap { boards =>
      trelloService.boardInfo(request.identity, boards)
    }

    (for {
      board <- boardInfoFuture
      summerized <- trelloService.summarizeInfo(board)
      dbUpdated <- boardService.saveAccumulatedData(summerized)
    } yield {
      Redirect(routes.Application.dashboard()).flashing("success" -> "Accumulation run with success")
    }).recoverWith {
      case e: Throwable =>
        Future.successful {
          Redirect(routes.Application.dashboard()).flashing("error" -> "Error while trying to run accumulation for today")
        }

    }
  }

}