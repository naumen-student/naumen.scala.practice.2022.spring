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
    case params: (String, String) => {
      Try(Await.result(controller.processResponse(controller.buildRequest(Option(params._1), Option(params._2))), 10 seconds)) match {
        case Success(value) => value match {
          case Some(list) => controller.updateDB(list)
          case None => print("Couldn't get list of jobs")
        }
        case Failure(_) => print("Failed response processing")
      }
    }
      print(s"tick with params ${params._1} ${params._2}\n")
    case _ => println("wrong receive")
  }
}