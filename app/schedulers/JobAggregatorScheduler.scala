package schedulers

import javax.inject.{Inject, Named}
import akka.actor.{ActorRef, ActorSystem}
import play.api.ConfigLoader._
import play.api.{Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class JobAggregatorScheduler @Inject()(val system: ActorSystem, @Named("job-actor") val jobsActor: ActorRef, configuration: Configuration)(implicit ec: ExecutionContext) {
  val logger = Logger("debug")

  val frequency = configuration.getOptional[Int]("request.frequency")
  val initialDelay = configuration.getOptional[Int]("request.initialDelay")
  val tagKeywords = configuration.getOptional[Seq[String]]("request.keywords.tags")
  val areaKeywords = configuration.getOptional[Seq[String]](s"request.keywords.areas")
  val zipped = tagKeywords.getOrElse{
    logger.warn("Empty list of keywords is being used for the scheduler")
    Seq[String]()
  }.zip(areaKeywords.getOrElse {
    logger.warn("Empty list of area names is being used for the scheduler")
    Seq[String]()
  })
  var actor = system.scheduler.schedule(
    initialDelay.getOrElse {
      logger.warn("Default initial delay is being used for the scheduler")
      5
    }.seconds,
    frequency.getOrElse{
      logger.warn("Default frequency is being used for the scheduler")
      5
    }.seconds, jobsActor, zipped)
}