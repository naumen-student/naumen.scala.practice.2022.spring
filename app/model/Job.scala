package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Job(id: String,
               title: String,
               requirements: Option[String],
               responsibility: Option[String],
               salaryFrom: Option[Int],
               salaryTo: Option[Int],
               salaryCurrency: Option[String],
               url: String)

object Job {
  implicit  val jobWrites: Writes[Job] = Json.writes[Job]

  implicit val jobReads: Reads[Job] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \\ "requirement").readNullable[String] and
      (JsPath \\ "responsibility").readNullable[String] and
      (JsPath \ "salary" \ "from").readNullable[Int] and
      (JsPath \ "salary" \ "to").readNullable[Int] and
      (JsPath \ "salary" \ "currency").readNullable[String] and
      (JsPath \ "alternate_url").read[String]
    )(Job.apply _)
}
