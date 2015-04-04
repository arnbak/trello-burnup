import actors.{AccumulationActor}
import akka.actor.{Props, ActorRef}
import filters.CORSFilter
import play.Logger
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}
import play.api.mvc.WithFilters
import scala.concurrent.duration._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object Global extends WithFilters(CORSFilter()) with GlobalSettings {

  override def onStart(app: Application) {

    val accumulationActor = Akka.system.actorOf(Props[AccumulationActor], name = "accumulationActor")
    Akka.system.scheduler.schedule(600 milliseconds, 5 hours, accumulationActor, "Accumulate")
  }

  override def onStop(app: Application) {

    val accumulationActor = Akka.system.actorOf(Props[AccumulationActor])
    Akka.system.stop(accumulationActor)

  }
}