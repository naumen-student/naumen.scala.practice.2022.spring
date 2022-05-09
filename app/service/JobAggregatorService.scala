package service

import model.{Job, Salary}
import model.db.DBTables.{jobTable, salaryTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.db.evolutions.InvalidDatabaseRevision
import play.api.libs.ws._
import play.api.libs.json.JsLookupResult.jsLookupResultToJsLookup
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class JobAggregatorService @Inject()(ws: WSClient,
                                     val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {


  def parse(text: String, area: Int) = {
    //&per_page=100&page=0
    val response = ws.url(s"https://api.hh.ru/vacancies?text=$text&area=$area").get()
    getRegions().flatMap(
      region => response.map(x => parseJobs(x.json, text, region._1(area))).flatMap(x => addToDBSeq(x._1, x._2)))
  }

  def parse(keyWords: Seq[String], areas: Seq[String]) = {
    val regionsMap = getRegions().map(x => x._2)

    val requests = for {
      keyWord <- keyWords
      area <- areas
    } yield regionsMap.map(x =>
      (ws.url(s"https://api.hh.ru/vacancies?text=$keyWord&area=${x(area)}"), keyWord, area))

    val jobsAndSalaries = for {
      request <- requests
    } yield request.flatMap(x => x._1.get().map(resp => parseJobs(resp.json, x._2, x._3)))

    jobsAndSalaries.foreach(x => x.map(s => addToDBSeq(s._1,s._2)))
  }

  private def parseJobs(json: JsValue, keyWord: String, area: String) = {
    val per_page = (json \ "per_page").get.toString.toInt
    var jobs = Seq[Job]()
    var salaries = Seq[Salary]()

    for (n <- 0 until per_page) {
      val items = (json \ "items" \ n).get
      val id = (items \ "id").as[String].toInt
      val name = (items \ "name").asOpt[String]
      val alternate_url = (items \ "alternate_url").asOpt[String]
      val requirement = (items \ "snippet" \ "requirement").asOpt[String]
      val responsibility = (items \ "snippet" \ "responsibility").asOpt[String]

      val salary = (items \ "salary").get
      var uuid: Option[UUID] = None
      if (salary != JsNull) {
        val currency = (salary \ "currency").asOpt[String]
        val from = (salary \ "from").asOpt[Int]
        val gross = (salary \ "gross").asOpt[Boolean]
        val to = (salary \ "to").asOpt[Int]
        val id = UUID.randomUUID()
        salaries = salaries :+ Salary(id, from, to, currency, gross)
        uuid = Option(id)
      }
      val job = Job(UUID.randomUUID(), id, name, requirement, responsibility, uuid, alternate_url, Option(area), Option(keyWord))
      job.hashCode()
      jobs = jobs :+ job
    }

    (jobs, salaries)
  }

  private def getRegions() = {
    val resp = ws.url("https://api.hh.ru/areas").get()
    val indexToRegion = scala.collection.mutable.Map[Int, String]()
    val regionToIndex = scala.collection.mutable.Map[String, Int]()

    def initialParse(json: JsValue) = {
      for (n <- json.as[Seq[JsValue]]) {
        parseRegions(n)
      }
      (indexToRegion, regionToIndex)
    }

    def parseRegions(regionJson: JsValue): Unit = {
      val areas = (regionJson \ "areas").as[Seq[JsValue]]
      val id = (regionJson \ "id").asOpt[String]
      val name = (regionJson \ "name").asOpt[String]

      if (id.isDefined && name.isDefined) {
        indexToRegion += (id.get.toInt -> name.get)
        regionToIndex += (name.get -> id.get.toInt)
      }

      if (areas.nonEmpty) {
        for (n <- areas)
          parseRegions(n)
      }
    }

    resp.map(x => initialParse(x.json))
  }

  private def addToDB(job: Job, salary: Salary) ={
    db.run(DBIO.seq(salaryTable+= salary, jobTable+=job))
  }

  private def addToDBSeq(jobs: Seq[Job], salaries: Seq[Salary]) = {
    db.run(DBIO.seq(salaryTable ++= salaries, jobTable ++= jobs))
    //Await.result(db.run(DBIO.seq(salaryTable ++= salaries, jobTable ++= jobs)), Duration.Inf)
  }
}
