package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws._
import service.JobAggregatorService

import scala.concurrent._
import scala.concurrent.duration._

import scala.util.{Failure, Success, Try}
import model.Job

import scala.language.postfixOps


@Singleton
class JobAggregatorController @Inject()(ws: WSClient,
      val controllerComponents: ControllerComponents,
      jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext) extends BaseController {

  def index() = Action {
    Ok("index page loaded")
  }

  def buildRequest(tag: Option[String], area: Option[String]): WSRequest = {
    var req = ws.url("https://api.hh.ru/vacancies")

    tag match {
      case Some(value) =>
        req = req.addQueryStringParameters("text" -> value)
        print(s"tag: $value\n")
      case None => print("tag: none")
    }

    area match {
      case Some(value) =>
        req = req.addQueryStringParameters("area" -> value)
        print(s"areaId: $value\n")
      case None => print("areaId: none\n")
    }
    req
  }

  def processResponse(request: WSRequest): Future[Option[List[Job]]] = {
    request.get().map {
      response => {
        Json.parse(response.body)("items") match {
          case result: JsValue => result.validate[List[Job]] match {
            case jobsResult: JsSuccess[List[Job]] => Option(jobsResult.get)
            case e: JsError => print(s"Validation failed, error message: ${e.toString}\n")
              None
          }
          case _ => print("Parsing failed!\n")
            None
        }
      }
    }
}

  def updateDB(jobs: List[Job]): Unit = {
    for (job <- jobs) Await.result(jobAggregatorService.addJob(job), Duration.Inf)
  }


  def loadJobs(tag: Option[String], area: Option[String]) = Action {
    implicit request: Request[AnyContent] => {
      Try(Await.result(processResponse(buildRequest(tag, area)), 10 seconds)) match {
        case Success(value) => value match       {
          case Some(list) => updateDB(list)
          case None => print("Couldn't get list of jobs")
        }
        case Failure(_) => print("Failed response processing")
      }
      Ok("Loading done")
    }
  }

  def showJobs() = Action {
    Ok(Json.toJson(Await.result(jobAggregatorService.getAllJobs(), Duration.Inf)))
  }
}
