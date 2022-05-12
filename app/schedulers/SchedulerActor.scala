package schedulers

import javax.inject.{Inject, Singleton}
import akka.actor.Actor
import org.joda.time.DateTime
import play.api.Logger

import scala.concurrent.{Await, ExecutionContext}
import service.JobAggregatorService

import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

@Singleton
class SchedulerActor @Inject()(jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext) extends Actor {
  override def receive: Receive = {
    case params: Seq[(String, String)] => {
      for(pair <- params) {
        Try(Await.result(jobAggregatorService.processJobResponse(jobAggregatorService.buildJobRequest(Some(pair._1), Some(pair._2))), Duration.Inf)) match {
          case Success(value) => value match {
            case Some(list) => for(task <- jobAggregatorService.addJobs(list, pair._2, pair._1)) Await.result(task, Duration.Inf)
            case None => print("Couldn't get list of jobs")
          }
          case Failure(_) => print("Failed job response processing\n")
        }
      }
    }
      print(s"tick with params $params\n")
    case _ => println("wrong receive")
  }
}