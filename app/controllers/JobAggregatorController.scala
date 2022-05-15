package controllers

import javax.inject._
import play.api.mvc._

import service.JobAggregatorService
import model._

import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent.ExecutionContext
import java.util.UUID
import play.api.libs.functional.syntax._

@Singleton
class JobAggregatorController @Inject()(ws: WSClient, val controllerComponents: ControllerComponents, jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext) extends BaseController {

  def getCity(areaNumber: String) = Action.async {
    ws.url(s"https://api.hh.ru/areas/$areaNumber").get().map { response =>
      val json = Json.parse(response.body)
      val city = (json \ "name").as[String]
      Ok(city)
    }

  }

  def getJobs(text: String, area: String) = Action.async { 
    ws.url(s"https://api.hh.ru/vacancies?text=$text&area=$area").get().map { response =>

      val json = Json.parse(response.body)

      val jobResult = json.validate[Jobs]


      jobResult match {
        case JsSuccess(jobs, _) => {
          // передаем jobs, text и areaNum в метод сервиса, где будет произведена запись в БД
          // в сервисе же будет запрашиваться город, возвращаться Future[String]

          jobAggregatorService.insertJobs(jobs)  
          
          Ok(Json.prettyPrint(Json.toJson(jobs)))
        }
        case e: JsError         => Ok(e.toString)
      }

    }
  }
}
