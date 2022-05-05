package schedulers

import javax.inject.{Inject, Named}
import akka.actor.{ActorRef, ActorSystem}
import play.api.ConfigLoader._
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class JobAggregatorScheduler @Inject()(val system: ActorSystem, @Named("job-actor") val jobsActor: ActorRef, configuration: Configuration)(implicit ec: ExecutionContext) {
  val frequency = configuration.get[Int]("request.frequency")
  val tagkeywords = configuration.get[Seq[String]]("request.keywords.tags")
  val areaKeywords = configuration.get[Seq[String]]("request.keywords.areas")
  var actor = system.scheduler.schedule(
    0.microseconds, frequency.seconds, jobsActor, tagkeywords.zip(areaKeywords))

}