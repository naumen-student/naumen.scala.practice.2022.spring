package model.db

import model.JobRequest
import slick.jdbc.PostgresProfile.api._

class JobRequestTable(tag: Tag) extends Table[JobRequest](tag, "jobrequesttable") {
  def jobid = column[String]("jobid", O.PrimaryKey)
  def city = column[String]("city")
  def keyword = column[String]("keyword")

  def * = (jobid, city, keyword) <> ((JobRequest.apply _).tupled, JobRequest.unapply)
}
