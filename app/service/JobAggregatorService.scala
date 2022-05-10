package service

import model.Job
import model.db.DBTables.jobTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject.{Inject, Singleton}
import play.api.libs.ws._
import play.api.libs.json.JsLookupResult.jsLookupResultToJsLookup
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}

@Singleton
class JobAggregatorService @Inject()(ws: WSClient,
                                     val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val perPage: Int = 100

  /**
   * добавляет в БД все вакансии по ключевому слову и региону
   * @param text ключевое слов
   * @param area индекс региона
   */
  def parse(text: String, area: Int) = {

    ws.url(s"https://api.hh.ru/vacancies?text=$text&area=$area&per_page=$perPage&page=0")
      .get()
      .flatMap(x => getJobs(x.json, text, area))
      .map(buff => buff.foreach(x => addToDB(x)))
  }

  /**
   * добавляет в БД все вакансии по всем ключевым словам и регионам
   * @param keyWords ключевые слова
   * @param areas индексы регионов
   */
  def parse(keyWords: Seq[String], areas: Seq[String]):Unit = {
    val regionsMap = getRegions().map(x => x._2)
    for {
      keyWord <- keyWords
      area <- areas
    } regionsMap.map(x => parse(keyWord, x(area)))
  }

  /**
   * делает несколько запросов к hh.ru, если все вакансии не помещаются в один ответ
   * @param firstResp первый ответ от hh.ru
   * @return Future от массива всех полученных вакансий
   */
  private def getJobs(firstResp: JsValue, keyWord: String, area: Int) = {

    def parseAllPages(areaText: String) = {
      val pages = (firstResp \ "pages").get.toString.toInt
      val jobs: ListBuffer[Job] = getJobsFromPage(firstResp, keyWord, areaText)

      val futures = for (n <- 1 until pages)
        yield ws.url(s"https://api.hh.ru/vacancies?text=$keyWord&area=$area&per_page=$perPage&page=$n")
          .get().map(resp => getJobsFromPage(resp.json, keyWord, areaText))

      Future.foldLeft(futures)(jobs)((acc, e) => acc ++ e)
    }

    getRegions().flatMap(x => parseAllPages(x._1(area)))
  }

  /**
   * парсит вакансии в массив DTO Job
   * @param json json, содержащий массив вакансий
   * @return массив DTO Job
   */
  private def getJobsFromPage(json: JsValue, keyWord: String, area: String): ListBuffer[Job] = {
    var jobs = ListBuffer[Job]()
    val items = (json \ "items").as[Seq[JsValue]]

    for (item <- items) {
      val id = (item \ "id").as[String].toInt
      val name = (item \ "name").asOpt[String]
      val alternate_url = (item \ "alternate_url").asOpt[String]
      val requirement = (item \ "snippet" \ "requirement").asOpt[String]
      val responsibility = (item \ "snippet" \ "responsibility").asOpt[String]

      val salary = (item \ "salary").get
      var from: Option[Int] = None
      var to: Option[Int] = None
      var currency: Option[String] = None
      var gross: Option[Boolean] = None

      if (salary != JsNull) {
        currency = (salary \ "currency").asOpt[String]
        from = (salary \ "from").asOpt[Int]
        gross = (salary \ "gross").asOpt[Boolean]
        to = (salary \ "to").asOpt[Int]
      }
      val job =
        Job(id, name, requirement, responsibility, alternate_url, from, to, currency, gross, Option(area), Option(keyWord))
      jobs += job
    }

    jobs
  }

  /**
   * @return возвращает два словаря: (id -> название_региона) и (название_региона -> id)
   */
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

  private def addToDB(job: Job) = {
    db.run(DBIO.seq(jobTable += job))
  }

  private def addToDBSeq(jobs: Seq[Job]) = {
    db.run(DBIO.seq(jobTable ++= jobs))
    //Await.result(db.run(DBIO.seq(salaryTable ++= salaries, jobTable ++= jobs)), Duration.Inf)
  }
}
