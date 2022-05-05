package model

import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Job(id: String, title: String, requirements: String, responsibility: String, salaryFrom: Int, salaryTo: Int, salaryCurr: String, url: String)


object Job {
  implicit val jobReads: Reads[Job] = (
      (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      ((JsPath \\ "requirement").read[String] or Reads.pure("")) and
      ((JsPath \\ "responsibility").read[String] or Reads.pure("")) and
      ((JsPath \ "salary" \ "from").read[Int] or Reads.pure(0)) and
      ((JsPath \ "salary" \ "to").read[Int] or Reads.pure(0)) and
      ((JsPath \ "salary" \ "currency").read[String] or Reads.pure("")) and
      (JsPath \ "alternate_url").read[String]
    )(Job.apply _)

  implicit  val jobWrites: Writes[Job] = (
      (JsPath \ "id").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "requirement").write[String] and
      (JsPath \ "responsibility").write[String] and
      (JsPath \ "salary" \ "from").write[Int] and
      (JsPath \ "salary" \ "to").write[Int] and
      (JsPath \ "salary" \ "currency").write[String] and
      (JsPath \ "url").write[String]
    )(unlift(Job.unapply))
}
