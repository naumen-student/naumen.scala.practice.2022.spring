package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import scheduler.{Scheduler, SchedulerActor}

class JobModule extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindActor[SchedulerActor]("scheduler-actor")
    bind(classOf[Scheduler]).asEagerSingleton()
  }
}
