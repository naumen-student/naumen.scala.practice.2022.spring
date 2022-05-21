package controllers

import model.Job

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import service.JobAggregatorService
import service.JobRequestService
import scala.concurrent._
import scala.language.postfixOps

@Singleton
class JobAggregatorController @Inject()(val controllerComponents: ControllerComponents,
                                        jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext, jobRequestService: JobRequestService) extends BaseController {

  def index() = Action.async {
    Future(Ok("Index page loaded"))
  }

  def loadJobs(keyword: Option[String], area: Option[String]) = Action.async {
    implicit request: Request[AnyContent] => {
      jobRequestService.getAreaId(area).flatMap(areaId =>
        jobRequestService.processJobResponse(jobRequestService.buildJobRequest(areaId, keyword)).flatMap {
          case Some(value) => jobAggregatorService.addJobs(value, area, keyword).flatMap(_ => Future(Ok(s"Loading of jobs with tags (${keyword.getOrElse("Empty")}, ${area.getOrElse("Empty")}) done")))
          case None => Future(Ok(s"Failed to load jobs with tags (${keyword.getOrElse("Empty")}, ${area.getOrElse("Empty")})"))
        })
    }
  }

  def showJobs() = Action.async {
    jobAggregatorService.getAllJobs().flatMap((jobs: Seq[Job]) => Future(Ok(Json.toJson(jobs))))
  }
}
