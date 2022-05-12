package service

import model.{Area, Job, JobRequest}
import model.db.DBTables.{jobRequestTable, jobTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration.{Duration, DurationInt}
import scala.language.postfixOps
import slick.dbio.DBIOAction

@Singleton
class JobAggregatorService @Inject()(ws: WSClient, val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
    def addJob(job: Job, area: String, keyword: String): Future[Unit] = {
        print(s"trying to insert job with id=${job.id}\n")
        db.run((for {
            _ <- jobTable.insertOrUpdate(job)
            _ <- jobRequestTable.insertOrUpdate(JobRequest(job.id, area, keyword))
        } yield ()).transactionally)
    }

    def addJobs(jobs: Seq[Job], area: String, keyword: String): Seq[Future[Unit]] = {
        for(job <- jobs)
            yield addJob(job, area, keyword)
    }

    def deleteJob(id: String): Future[Int] = {
        db.run(jobTable.filter(_.id === id).delete)
    }

    def getJob(id: String): Future[Option[Job]] = {
        db.run(jobTable.filter(_.id === id).result.headOption)
    }

    def getAllJobs() : Future[Seq[Job]] = {
        db.run(jobTable.result)
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
}
