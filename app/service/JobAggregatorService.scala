package service

import model.{Area, Job, JobRequest}
import model.db.DBTables.{jobRequestTable, jobTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import play.api.Logger

@Singleton
class JobAggregatorService @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{
    val logger = Logger("debug")
    def addJob(job: Job, area: Option[String], keyword: Option[String]): Future[Unit] = {
        logger.info(s"Trying to insert job with id: ${job.id} from request: $area $keyword\n")
        logger.info(job.toString)
        db.run((for {
            _ <- jobTable.insertOrUpdate(job)
            _ <- jobRequestTable.insertOrUpdate(JobRequest(job.id, area, keyword))
        } yield ()).transactionally)
    }

    def addJobs(jobs: Seq[Job], area: Option[String], keyword: Option[String]): Future[Seq[Unit]] = {
        Future.traverse(jobs)(job => addJob(job, area, keyword))
    }

    def deleteJob(id: String): Future[Int] = {
        logger.info(s"Deleting job with id: $id")
        db.run(jobTable.filter(_.id === id).delete)
    }

    def getJob(id: String): Future[Option[Job]] = {
        db.run(jobTable.filter(_.id === id).result.headOption)
    }

    def getAllJobs() : Future[Seq[Job]] = {
        db.run(jobTable.result)
    }
}
