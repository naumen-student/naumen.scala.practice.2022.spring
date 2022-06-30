package model

case class Job(id: Int,
               title: Option[String],
               requirement: Option[String],
               responsibility: Option[String],
               alternateUrl: Option[String],
               salaryFrom: Option[Int],
               salaryTo: Option[Int],
               salaryCurrency: Option[String],
               salaryGross: Option[Boolean],
               city: Option[String],
               keyWord: Option[String])
