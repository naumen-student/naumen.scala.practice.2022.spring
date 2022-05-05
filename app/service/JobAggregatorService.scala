package service

import model.Job
import model.db.DBTables.jobTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class JobAggregatorService @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
    def addJob(job: Job): Future[Int] = {
        val insertionAction = jobTable.insertOrUpdate(job)
        print(s"trying to insert job with id=${job.id}\n")
        db.run(insertionAction)
    }

    def deleteJob(id: String): Future[Int] = {
        val deletionAction = jobTable.filter(_.id === id).delete
        dbConfig.db.run(deletionAction)
    }

    def getJob(id: String): Future[Option[Job]] = {
        val filterAction = jobTable.filter(_.id === id).result.headOption
        dbConfig.db.run(filterAction)
    }

    def getAllJobs() : Future[Seq[Job]] = {
        val getAllAction = jobTable.result
        db.run(getAllAction)
    }
}
