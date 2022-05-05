package model.db

import model.Job
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

class JobTable(tag: Tag) extends Table[Job](tag, "jobtable") {
    def id = column[String]("id", O.PrimaryKey)
    def title = column[String]("title")
    def requirements = column[String]("requirements")
    def responsibility = column[String]("responsibility")
    def salaryFrom = column[Int]("salaryfrom")
    def salaryTo = column[Int]("salaryto")
    def salaryCurr = column[String]("salarycurr")
    def url = column[String]("url")


    def * = (id, title, requirements, responsibility, salaryFrom, salaryTo, salaryCurr, url) <> ((Job.apply _).tupled, Job.unapply)
}

