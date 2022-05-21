package schedulers

import javax.inject.{Inject, Singleton}
import akka.actor.Actor
import play.api.Logger
import service.JobRequestService

import scala.concurrent.{Await, ExecutionContext, Future}
import service.JobAggregatorService

import scala.concurrent.duration.Duration
import scala.language.postfixOps

@Singleton
class SchedulerActor @Inject()(jobRequestService: JobRequestService, jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext)
  extends Actor {
  val logger = Logger("debug")

  override def receive: Receive = {
    case params: Seq[(String, String)] =>
      params.map(pair => {
        var area = pair._2
        var keyword = pair._1
        logger.info(s"Scheduler tick with params $area $keyword\n")
        jobRequestService.getAreaId(Some(area)).flatMap(areaId =>
          jobRequestService.processJobResponse(jobRequestService.buildJobRequest(areaId, Some(keyword))).flatMap({
            case Some(value) => jobAggregatorService.addJobs(value, Some(area), Some(keyword)).flatMap(_ => Future())
            case None => logger.error(s"Couldn't get list of jobs for params $area $keyword\n")
              Future()
          }))
      })
    case _ => logger.error(s"Wrong receive message!")
  }
}