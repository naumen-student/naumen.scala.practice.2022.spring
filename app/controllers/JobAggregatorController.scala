package controllers

import javax.inject._
import play.api.mvc._
import service.JobAggregatorService
import scala.util.{Failure, Success}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JobAggregatorController @Inject()(val controllerComponents: ControllerComponents,
                                        jobAggregatorService: JobAggregatorService)(implicit ec: ExecutionContext) extends BaseController {

  def index(text: String, area: Int) = Action.async { implicit request: Request[AnyContent] =>

    jobAggregatorService.parse(text, area).map(_ => Ok(""))
      .recover(x => InternalServerError("Some exception has occurred"))
  }
}
