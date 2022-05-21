package model.db

import model.Job
import slick.jdbc.PostgresProfile.api._

class JobTable(tag: Tag) extends Table[Job](tag, "job") {
  def id = column[Int]("id", O.PrimaryKey)

  def title = column[Option[String]]("title")

  def requirement = column[Option[String]]("requirement")

  def responsibility = column[Option[String]]("responsibility")

  def alternateUrl = column[Option[String]]("alternate_url")

  def salaryFrom = column[Option[Int]]("salary_from")

  def salaryTo = column[Option[Int]]("salary_to")

  def salaryCurrency = column[Option[String]]("salary_currency")

  def salaryGross = column[Option[Boolean]]("salary_gross")

  def city = column[Option[String]]("city")

  def keyWord = column[Option[String]]("key_word")


  def * =
    (id, title, requirement, responsibility, alternateUrl, salaryFrom, salaryTo, salaryCurrency, salaryGross,
      city, keyWord) <> (Job.tupled, Job.unapply)
}

