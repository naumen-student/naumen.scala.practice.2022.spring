package model

import java.util.UUID

case class Job(id: UUID,
               hh_id: Int,
               title: Option[String],
               requirement: Option[String],
               responsibility: Option[String],
               salary_id: Option[UUID],
               alternate_url: Option[String],
               city: Option[String],
               key_word: Option[String])
