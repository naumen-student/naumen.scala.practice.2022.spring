package scheduler

import javax.inject.Inject
import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.{Configuration, Logger, Mode}
import play.libs.XML.Constants
import service.JobAggregatorService

import scala.util.Try

class Task @Inject()(actorSystem: ActorSystem,
                     configuration: Configuration,
                     jobAggregatorService: JobAggregatorService)(implicit executionContext: ExecutionContext) {

  val logger: Logger = Logger("Scheduler")

  val initialDelay: String = configuration.getOptional[String]("initialDelay")
    .getOrElse(sys.error("No \"initialDelay\" field found or it has wrong type"))

  val interval: String = configuration.getOptional[String]("interval")
    .getOrElse(sys.error("No \"interval\" field found or it has wrong type"))

  val cities: Seq[String] = configuration.getOptional[Seq[String]]("cities")
    .getOrElse(sys.error("No \"cities\" field found or it has wrong type"))

  val keyWords: Seq[String] = configuration.getOptional[Seq[String]]("keyWords")
    .getOrElse(sys.error("No \"keyWords\" field found or it has wrong type"))


  val initDelay: FiniteDuration = Try(Duration(initialDelay).asInstanceOf[FiniteDuration])
    .getOrElse(sys.error("\"initialDelay\" field has wrong format"))
  val interv: FiniteDuration = Try(Duration(interval).asInstanceOf[FiniteDuration])
    .getOrElse(sys.error("\"interval\" field has wrong format"))

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = initDelay,
    interval = interv) { () =>
    jobAggregatorService.aggregateData(keyWords, cities)
    logger.info("Scheduled task executed")
  }
}
