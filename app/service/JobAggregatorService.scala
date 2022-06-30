package service

import DTOs._
import model.Job
import model.db.DBTables.jobTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger, Mode}
import play.api.libs.ws._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}


@Singleton
class JobAggregatorService @Inject()(ws: WSClient,
                                     configuration: Configuration,
                                     val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  val logger: Logger = Logger("JobAggregatorService")
  val perPage: Int = configuration.getOptional[Int]("perPage") match {
    case Some(value) => value
    case None =>
      logger.warn("perPage is not configured. The default value(100) will be used")
      100
  }

  /**
   * агрегирует вакансии по ключевому слову и региону
   *
   * @param text ключевое слов
   * @param area индекс региона
   */
  def aggregateData(text: String, area: Int) = {

    ws.url(s"https://api.hh.ru/vacancies?text=$text&area=$area&per_page=$perPage&page=0")
      .get()
      .flatMap(response => getJobs(response.json, text, area))
      .map(buff => buff.foreach(job => addToDB(job)))
  }

  /**
   * агрегирует вакансии по ключевому слову и региону
   *
   * @param keyWords ключевые слова
   * @param areas    индексы регионов
   */
  def aggregateData(keyWords: Seq[String], areas: Seq[String]): Unit = {
    getRegions() match {
      case Success(value) => {
        val regionsMap = value.map(x => x._2)
        for {
          keyWord <- keyWords
          area <- areas
        } regionsMap.map(x => aggregateData(keyWord, x(area)))
      }
      case Failure(exception) => logger.error(exception.getMessage, exception)
    }
  }

  /**
   * делает несколько запросов к hh.ru, если все вакансии не помещаются в один ответ
   *
   * @param firstResp первый ответ от hh.ru
   * @return Future от массива всех полученных вакансий
   */
  private def getJobs(firstResp: JsValue, keyWord: String, area: Int): Future[List[Job]] = {

    def handleJobs(jobs: Try[List[Job]]) = {
      jobs match {
        case Success(value) => value
        case Failure(exception) => logger.error(exception.getMessage, exception)
          Nil
      }
    }

    def parseAllPages(areaText: String): Future[List[Job]] = {
      val pages = firstResp.asOpt[HolderDTO] match {
        case Some(holder) => holder.pages
        case None => return Future.failed(new ClassCastException("json can't be parsed: " + firstResp.toString))
      }
      val jobs: List[Job] = handleJobs(getJobsFromPage(firstResp, keyWord, areaText))

      val requests = for (n <- 1 until pages)
        yield ws.url(s"https://api.hh.ru/vacancies?text=$keyWord&area=$area&per_page=$perPage&page=$n")

      requests.foldLeft(Future.successful(jobs))((fut, req) =>
        req.get().map(resp => handleJobs(getJobsFromPage(resp.json, keyWord, areaText)))
          .flatMap(newJobs => fut.map(oldJobs => oldJobs ++ newJobs)))
    }

    getRegions() match {
      case Success(value) => value.flatMap(x => parseAllPages(x._1(area)))
      case Failure(exception) => logger.error(exception.getMessage, exception)
        Future.failed(exception)
    }
  }

  /**
   * парсит вакансии в массив DTO Job
   *
   * @param json json, содержащий массив вакансий
   * @return массив DTO Job
   */
  private def getJobsFromPage(json: JsValue, keyWord: String, area: String): Try[List[Job]] = {
    var jobs = ListBuffer[Job]()

    def addJob(item: VacancyDTO): Unit = {
      var requirement: Option[String] = None
      var responsibility: Option[String] = None

      item.snippet match {
        case Some(value) => {
          requirement = value.requirement
          responsibility = value.responsibility
        }
        case _ =>
      }

      Try(item.id.toInt).toOption match {
        case Some(id) => item.salary match {
          case Some(salary) => jobs += Job(id, item.name, requirement, responsibility,
            item.alternate_url, salary.from, salary.to, salary.currency, salary.gross, Option(area), Option(keyWord))
          case None => jobs += Job(id, item.name, requirement, responsibility,
            item.alternate_url, None, None, None, None, Option(area), Option(keyWord))
        }
        case None => throw new NumberFormatException("id value can't be converted to int" + json.toString())
      }
    }

    Try {
      json.asOpt[HolderDTO] match {
        case Some(holder) => holder.items match {
          case Some(items) => items.foreach(addJob)
          case None => throw new RuntimeException("Items doesn't exist" + json.toString())
        }
        case None => throw new RuntimeException("Can't be parsed to HolderDTO" + json.toString())
      }

      jobs.toList
    }
  }

  /**
   * @return возвращает два словаря: (id -> название_региона) и (название_региона -> id)
   */
  private def getRegions() = {
    val resp = ws.url("https://api.hh.ru/areas").get()
    val indexToRegion = scala.collection.mutable.Map[Int, String]()
    val regionToIndex = scala.collection.mutable.Map[String, Int]()


    def initialParse(json: JsValue) = {
      json.asOpt[Seq[RegionDTO]] match {
        case Some(regions) => regions.foreach(parseRegions)
        case None => throw new ClassCastException("Can't be parsed to Seq[RegionDTO]: " + json.toString())
      }
      (indexToRegion.toMap, regionToIndex.toMap)
    }

    def parseRegions(regionDTO: RegionDTO): Unit = {
      Try(regionDTO.id.toInt).toOption match {
        case Some(id) => {
          indexToRegion += (id -> regionDTO.name)
          regionToIndex += (regionDTO.name -> id)
          if (regionDTO.areas.nonEmpty) {
            regionDTO.areas.foreach(area => parseRegions(area))
          }
        }
        case None => throw new NumberFormatException("region id can't be converted to int")
      }
    }

    Try(resp.map(x => initialParse(x.json)))
  }

  private def addToDB(job: Job) = {
    db.run(DBIO.seq(jobTable += job))
  }
}
