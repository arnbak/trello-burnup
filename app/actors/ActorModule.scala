package actors

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorModule extends AbstractModule with AkkaGuiceSupport {

  def configure() = {
    bindActor[AccumulationActor]("accumulation-actor")
  }
}