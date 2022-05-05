package schedulers

import javax.inject.{Inject, Singleton}
import akka.actor.Actor
import org.joda.time.DateTime
import play.api.Logger

import scala.concurrent.{Await, ExecutionContext}
import controllers.JobAggregatorController

import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

@Singleton
class SchedulerActor @Inject()(controller: JobAggregatorController)(implicit ec: ExecutionContext) extends Actor {
  override def receive: Receive = {
    case params: Seq[(String, String)] => {
      for(pair <- params)
      Try(Await.result(controller.processResponse(controller.buildRequest(Option(pair._1), Option(pair._2))), 10 seconds)) match {
        case Success(value) => value match {
          case Some(list) => controller.updateDB(list)
          case None => print("Couldn't get list of jobs")
        }
        case Failure(_) => print("Failed response processing")
      }
    }
      print(s"tick with params $params\n")
    case _ => println("wrong receive")
  }
}