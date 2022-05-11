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
import model.Area

import scala.language.postfixOps


@Singleton
class JobAggregatorController @Inject()(ws: WSClient,
      val controllerComponents: ControllerComponents,
      jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext) extends BaseController {

  def index() = Action {
    Ok("index page loaded")
  }

  def buildJobRequest(tag: Option[String], area: Option[String]): WSRequest = {
    var req = ws.url("https://api.hh.ru/vacancies")

    tag match {
      case Some(value) =>
        req = req.addQueryStringParameters("text" -> value)
        print(s"tag: $value\n")
      case None => print("tag: none")
    }

    area match {
      case Some(areaName) =>
        val areaId = getRegionId(areaName)
        req = req.addQueryStringParameters("area" -> areaId)
        print(s"areaId: $areaId\n")
      case None => print("areaId: none\n")
    }
    req
  }

  def processJobResponse(request: WSRequest): Future[Option[List[Job]]] = {
    request.get().map {
      response => {
        Json.parse(response.body)("items") match {
          case result: JsValue => result.validate[List[Job]] match {
            case jobsResult: JsSuccess[List[Job]] => Some(jobsResult.get)
            case e: JsError => print(s"Validation of jobs failed, error message: ${e.toString}\n")
              None
          }
          case _ => print("Parsing of jobs failed!\n")
            None
        }
      }
    }
}

  def updateDB(jobs: List[Job]): Unit = {
    for (job <- jobs) Await.result(jobAggregatorService.addJob(job), Duration.Inf)
  }

  def parseRegions(regionJson: JsValue): Option[List[Area]] = {
      regionJson.validate[List[Area]] match {
        case areaResult: JsSuccess[List[Area]] =>
          var areas = areaResult.get
          for(area <- areaResult.get)
            area.children match {
              case Some(children) => areas = areas ++ parseRegions(children).getOrElse(List[Area]())
              case None => print("No children")
            }
          Some(areas)
        case e: JsError => print(s"Validation of regions failed, error message: ${e.toString}\n")
        None
      }
  }

  def getRegionId(name: String): String =
  {
    def getRegionIdFuture(name: String) =
      ws.url("https://api.hh.ru/areas").get().map {
        response => {
          Json.parse(response.body) match {
            case result: JsValue => parseRegions(result) match {
              case Some(areaList) => areaList
              case None => null
            }
            case _ => null
          }
        }
      }

    Try(Await.result(getRegionIdFuture(name), Duration.Inf)) match {
      case Success(list) => list.find(_.name.equalsIgnoreCase(name)) match {
        case Some(value) => value.id
        case None => print(s"Couldn't find id for ${name}\n")
          null
      }
      case Failure(_) => print("Didnt finish getting id\n")
        null
    }
  }


  def loadJobs(tag: Option[String], area: Option[String]) = Action {
    implicit request: Request[AnyContent] => {
          Try(Await.result(processJobResponse(buildJobRequest(tag, area)), Duration.Inf)) match {
            case Success(value) => value match {
              case Some(list) => updateDB(list)
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
