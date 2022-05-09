package model.db

import java.util.UUID

import model.Salary
import slick.jdbc.PostgresProfile.api._

class SalaryTable(tag: Tag) extends Table[Salary](tag, "salary") {
  def id = column[UUID]("id", O.PrimaryKey)

  def _from = column[Option[Int]]("_from")

  def _to = column[Option[Int]]("_to")

  def currency = column[Option[String]]("currency")

  def gross = column[Option[Boolean]]("gross")

  override def * = (id, _from, _to, currency, gross) <> (Salary.tupled, Salary.unapply)
}
