package scheduler

import service.JobAggregatorService

import javax.inject.{Inject, Named}

import akka.actor.{ActorRef, ActorSystem}
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Scheduler @Inject() (val system: ActorSystem, @Named("scheduler-actor") val schedulerActor: ActorRef, configuration: Configuration, jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext) {

  val initialDelay = configuration.get[String]("schedule.initialDelay")
  val interval = configuration.get[String]("schedule.interval")
  val cities = configuration.get[Seq[String]]("schedule.cities")
  val keyWords = configuration.get[Seq[String]]("schedule.keyWords")

  var actor = system.scheduler.schedule(
    Duration(initialDelay).asInstanceOf[FiniteDuration],
    Duration(interval).asInstanceOf[FiniteDuration], 
    schedulerActor, "update")

}
