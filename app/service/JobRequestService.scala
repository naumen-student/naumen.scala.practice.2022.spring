package service

import model._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class JobRequestService @Inject()(ws: WSClient)(implicit ec: ExecutionContext) {
  val logger: Logger = Logger("debug")

  def buildJobRequest(area: Option[String], keyword: Option[String]): WSRequest = {
    logger.info("Building request...")
    logger.info(s"Keyword: ${keyword.getOrElse("none")}")
    logger.info(s"Area: ${area.getOrElse("none")}")

    ws.url("https://api.hh.ru/vacancies").addQueryStringParameters {
      keyword match {
        case Some(value) => "text" -> value
        case None => "text" -> null
      }
      area match {
        case Some(value) => "area" -> value
        case None => "text" -> null
      }
    }
  }

  def processJobResponse(request: WSRequest): Future[Option[List[Job]]] = {
    request.get().map(response => Json.parse(response.body)("items") match {
      case result: JsValue => result.asOpt[List[Job]]
      case _ => logger.error(s"Couldn't parse jobs json, ${response.body}")
        None
    })
  }

  def getAreaId(name: Option[String]): Future[Option[String]] = {
    name match {
      case Some(value) => ws.url("https://api.hh.ru/areas").get().map {
        response =>
          Json.parse(response.body) match {
            case result: JsValue => result.asOpt[List[Area]] match {
              case Some(areas) => areas.foldLeft(List[Area]())((accumulator, area) => accumulator ++ area.areas.getOrElse(List[Area]()))
                .find(_.name == value).map(_.id)
              case None => logger.error(s"Parsing of areas failed, ${response.body}")
                None
            }
            case _ => logger.error(s"Couldn't parse areas json, ${response.body}")
              None
          }
      }
      case None => logger.info(s"No area name to translate to area id")
        Future(None)
    }
  }
}
