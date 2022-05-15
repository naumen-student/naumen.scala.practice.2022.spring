package service

import model.{Job, Jobs}
import model.db.DBTables.jobTable
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class JobAggregatorService @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

    // all queries here
    
    def insertJobs(jobs: Jobs) = {
        val result = db.run(jobTable ++= jobs.items)
        Await.result(result, Duration.Inf)
    }

    // def insertJob = {
    //     val result = db.run(jobTable += Job("1", "name", Some("resp"), Some("ded"), "url"))
    //     Await.result(result, Duration.Inf)
    // }

}
