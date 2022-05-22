package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers.{JobAggregatorScheduler, SchedulerActor}

class JobModule extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindActor[SchedulerActor]("job-actor")
    bind(classOf[JobAggregatorScheduler]).asEagerSingleton()
  }
}