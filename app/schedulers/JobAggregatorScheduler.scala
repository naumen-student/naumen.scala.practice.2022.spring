package schedulers

import javax.inject.{Inject, Named}
import akka.actor.{ActorRef, ActorSystem}
import play.api
import play.api.ConfigLoader.intLoader
import play.api.ConfigLoader.stringLoader
import play.api.{ConfigLoader, Configuration, Logger}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class JobAggregatorScheduler @Inject()(val system: ActorSystem, @Named("job-actor") val jobsActor: ActorRef, configuration: Configuration)(implicit ec: ExecutionContext) {
  val frequency = configuration.get[Int]("request.frequency")
  val tagKeyword = configuration.get[String]("request.keywords.tag")
  val areaKeyword = configuration.get[String]("request.keywords.area")
  var actor = system.scheduler.schedule(
    0.microseconds, frequency.seconds, jobsActor, (tagKeyword, areaKeyword))

}