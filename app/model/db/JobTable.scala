package model.db

import model.Job
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

class JobTable(tag: Tag) extends Table[Job](tag, "jobtable") {
    def db_id = column[UUID]("db_id", O.PrimaryKey, O.AutoInc)
    def id = column[String]("id")
    def title = column[String]("title")
    def requirements = column[String]("requirements")
    def responsibility = column[String]("responsibility")
    def salary = column[String]("salary")
    def url = column[String]("url")


    def * = (db_id, id, title, requirements, responsibility, salary, url) <> ((Job.apply _).tupled, Job.unapply)
}

