package model.db

import model.JobRequest
import slick.jdbc.PostgresProfile.api._

class JobRequestTable(tag: Tag) extends Table[JobRequest](tag, "jobrequesttable") {
  def jobId = column[String]("jobid", O.PrimaryKey)
  def city = column[Option[String]]("city")
  def keyword = column[Option[String]]("keyword")

  def * = (jobId, city, keyword) <> ((JobRequest.apply _).tupled, JobRequest.unapply)
}
