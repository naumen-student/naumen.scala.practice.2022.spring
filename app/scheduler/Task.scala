package scheduler

import javax.inject.Inject
import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import play.api.{Configuration, Logger, Mode}
import service.JobAggregatorService

import scala.util.Try

class Task @Inject()(actorSystem: ActorSystem,
                     configuration: Configuration,
                     jobAggregatorService: JobAggregatorService)(implicit executionContext: ExecutionContext) {

  if (!configuration.has("initialDelay") || !configuration.has("interval")
    || !configuration.has("cities") || !configuration.has("keyWords"))
    throw new NoSuchFieldException("Configuration doesn't have some of these paths:" +
      " initialDelay, interval, cities, keyWords")

  val initialDelay: Option[String] = configuration.getOptional[String]("initialDelay")
  val interval: Option[String] = configuration.getOptional[String]("interval")
  val cities: Option[Seq[String]] = configuration.getOptional[Seq[String]]("cities")
  val keyWords: Option[Seq[String]] = configuration.getOptional[Seq[String]]("keyWords")

  if (initialDelay.isEmpty || interval.isEmpty || cities.isEmpty || keyWords.isEmpty)
    throw new ClassCastException("Some of these paths have wrong type: initialDelay, interval, cities, keyWords")

  val initDelay: Try[FiniteDuration] = Try(Duration(initialDelay.get).asInstanceOf[FiniteDuration])
  val interv: Try[FiniteDuration] = Try(Duration(interval.get).asInstanceOf[FiniteDuration])

  if(initDelay.isFailure || interv.isFailure)
    throw new ClassCastException("Initial delay or interval have wrong format")

  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = initDelay.get,
    interval = interv.get) { () =>
    jobAggregatorService.aggregateData(keyWords.get, cities.get)
    Logger("play").info("Scheduled task executed")
  }
}
