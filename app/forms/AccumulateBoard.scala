package forms

import play.api.data.Form
import play.api.data.Forms._

case class AccumulateBoardList(boards: List[AccumulateBoardForm])
case class AccumulateBoardForm(boardId: String, boardName: String, selected: Boolean)

object AccumulateBoard {

  val boardList = Form(
    mapping(
      "boards" -> list(mapping(
        "boardId" -> nonEmptyText,
        "boardName" -> nonEmptyText,
        "selected" -> boolean
      )(AccumulateBoardForm.apply)(AccumulateBoardForm.unapply))
    )(AccumulateBoardList.apply)(AccumulateBoardList.unapply)
  )
}

