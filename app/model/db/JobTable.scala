package model.db

import model.Job
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

class JobTable(tag: Tag) extends Table[Job](tag, "job") {
  def id = column[Int]("id", O.PrimaryKey)

  def title = column[Option[String]]("title")

  def requirement = column[Option[String]]("requirement")

  def responsibility = column[Option[String]]("responsibility")

  def alternate_url = column[Option[String]]("alternate_url")

  def salary_from = column[Option[Int]]("salary_from")

  def salary_to = column[Option[Int]]("salary_to")

  def salary_currency = column[Option[String]]("salary_currency")

  def salary_gross = column[Option[Boolean]]("salary_gross")

  def city = column[Option[String]]("city")

  def key_word = column[Option[String]]("key_word")


  def * =
    (id, title, requirement, responsibility, alternate_url, salary_from, salary_to, salary_currency, salary_gross,
      city, key_word) <> (Job.tupled, Job.unapply)
}

