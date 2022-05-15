package model.db

import model.Job
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

class JobTable(tag: Tag) extends Table[Job](tag, "jobs") {
    def id = column[String]("id", O.PrimaryKey)
    def title = column[String]("title")
    def requirement = column[Option[String]]("requirement")
    def responsibility = column[Option[String]]("responsibility")
    def url = column[String]("url")


    def * = (id, title, requirement, responsibility, url) <> ((Job.apply _).tupled, Job.unapply)
}

