package model.db

import model.Job
import slick.jdbc.PostgresProfile.api._
import java.util.UUID

import model.db.DBTables.salaryTable

class JobTable(tag: Tag) extends Table[Job](tag, "job") {
  def id = column[UUID]("id", O.PrimaryKey)

  def hh_id = column[Int]("hh_id")

  def title = column[Option[String]]("title")

  def requirement = column[Option[String]]("requirement")

  def responsibility = column[Option[String]]("responsibility")

  def salary_id = column[Option[UUID]]("salary_id")

  def alternate_url = column[Option[String]]("alternate_url")

  def city = column[Option[String]]("city")

  def key_word = column[Option[String]]("key_word")

  def salary = foreignKey("fk_1", salary_id, salaryTable)(_.id)


  def * =
    (id, hh_id, title, requirement, responsibility, salary_id, alternate_url, city, key_word) <> (Job.tupled, Job.unapply)
}

