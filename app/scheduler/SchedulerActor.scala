package scheduler

import javax.inject.{Inject, Singleton}

import akka.actor.Actor
import org.joda.time.DateTime
import play.api.Logger

import scala.concurrent.ExecutionContext

@Singleton
class SchedulerActor @Inject()()(implicit ec: ExecutionContext) extends Actor {
  override def receive: Receive = {
    case _ =>
      // your job here
  }
}
