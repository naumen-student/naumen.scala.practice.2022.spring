package scheduler

import javax.inject.Inject
import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.Configuration
import service.JobAggregatorService

class Task @Inject()(actorSystem: ActorSystem,
                     configuration: Configuration,
                     jobAggregatorService: JobAggregatorService)(implicit executionContext: ExecutionContext) {

  val initialDelay: String = configuration.get[String]("initialDelay")
  val interval: String = configuration.get[String]("interval")
  val cities: Seq[String] = configuration.get[Seq[String]]("cities")
  val keyWords: Seq[String] = configuration.get[Seq[String]]("keyWords")

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = Duration(initialDelay).asInstanceOf[FiniteDuration],
    interval = Duration(interval).asInstanceOf[FiniteDuration]) { () =>
    jobAggregatorService.parse(keyWords, cities)
    println("Scheduled task executed")
    //actorSystem.log.info("Executing something...")
  }
}
