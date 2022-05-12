package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import service.JobAggregatorService
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.language.postfixOps

@Singleton
class JobAggregatorController @Inject()(
      val controllerComponents: ControllerComponents,
      jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext) extends BaseController {

  def index() = Action {
    Ok("index page loaded")
  }


  def loadJobs(tag: Option[String], area: Option[String]) = Action {
    implicit request: Request[AnyContent] => {
          Try(Await.result(jobAggregatorService.processJobResponse(jobAggregatorService.buildJobRequest(tag, area)), Duration.Inf)) match {
            case Success(value) => value match {
              case Some(list) => for(task <- jobAggregatorService.addJobs(list, area.getOrElse(""), tag.getOrElse(""))) Await.result(task, Duration.Inf)
              case None => print("Couldn't get list of jobs")
            }
            case Failure(_) => print("Failed job response processing")
          }
          Ok("Loading done")
      }
    }

  def showJobs() = Action {
    Ok(Json.toJson(Await.result(jobAggregatorService.getAllJobs(), Duration.Inf)))
  }
}
