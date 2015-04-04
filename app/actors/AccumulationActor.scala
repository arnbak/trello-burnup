package actors

import akka.actor.Actor
import controllers.TrelloController

import play.Logger
import play.api.db.slick.DB
import play.api.Play.current

class AccumulationActor extends Actor {

  def receive =  {
    case "Accumulate" => {
      Logger.info("Accumulate")
      TrelloController.accumulate
    }
    case _ => Logger.info("Unhandled Message")
  }

}
