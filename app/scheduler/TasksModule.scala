package scheduler

import play.api.inject.SimpleModule
import play.api.inject._

class TasksModule extends SimpleModule(bind[Task].toSelf.eagerly())
