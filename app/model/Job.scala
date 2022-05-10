package model

import java.util.UUID

case class Job(id: Int,
               title: Option[String],
               requirement: Option[String],
               responsibility: Option[String],
               alternate_url: Option[String],
               salary_from: Option[Int],
               salary_to: Option[Int],
               salary_currency: Option[String],
               salary_gross: Option[Boolean],
               city: Option[String],
               key_word: Option[String])
