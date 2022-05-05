package model

import java.util.UUID
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Job(db_id: UUID, id: String, title: String, requirements: String, responsibility: String, salary: String, url: String)

object Job {
  implicit val jobReads: Reads[Job] = (
    Reads.pure(UUID.randomUUID()) and
      (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      ((JsPath \\ "requirement").read[String] or Reads.pure("")) and
      ((JsPath \\ "responsibility").read[String] or Reads.pure("")) and
      ((JsPath \ "salary").read[String] or Reads.pure("")) and
      (JsPath \ "alternate_url").read[String]
    )(Job.apply _)

  implicit  val jobWrites: Writes[Job] = (
    (JsPath \ "db_id").write[UUID] and
      (JsPath \ "id").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "requirement").write[String] and
      (JsPath \ "responsibility").write[String] and
      (JsPath \ "salary").write[String] and
      (JsPath \ "url").write[String]
    )(unlift(Job.unapply))
}
