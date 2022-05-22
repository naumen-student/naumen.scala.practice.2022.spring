package model.db

import model.Job
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

class JobTable(tag: Tag) extends Table[Job](tag, "jobtable") {
    def id = column[String]("id", O.PrimaryKey)
    def title = column[String]("title")
    def requirements = column[Option[String]]("requirements")
    def responsibility = column[Option[String]]("responsibility")
    def salaryFrom = column[Option[Int]]("salaryfrom")
    def salaryTo = column[Option[Int]]("salaryto")
    def salaryCurrency = column[Option[String]]("salarycurr")
    def url = column[String]("url")


    def * = (id, title, requirements, responsibility, salaryFrom, salaryTo, salaryCurrency, url) <> ((Job.apply _).tupled, Job.unapply)
}

