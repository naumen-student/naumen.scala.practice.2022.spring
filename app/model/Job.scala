package model

import java.util.UUID

import play.api.libs.json._
import play.api.libs.functional.syntax._


case class Job(id: String, title: String, requirement: Option[String], responsibility: Option[String], url: String)

case class Jobs(items: Seq[Job])

object Job {
  implicit val jobReads: Reads[Job] = (
      (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "snippet" \ "requirement").readNullable[String] and
      (JsPath \ "snippet" \ "responsibility").readNullable[String] and
      (JsPath \ "alternate_url").read[String]
    )(Job.apply _)

  implicit val jobWrites: Writes[Job] = (
      (JsPath \ "id").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "snippet" \ "requirement").writeNullable[String] and
      (JsPath \ "snippet" \ "responsibility").writeNullable[String] and
      (JsPath \ "alternate_url").write[String]
    )(unlift(Job.unapply))
}

object Jobs {
  implicit val itemsReads: Reads[Jobs] = 
    (JsPath \ "items").read[Seq[Job]].map(Jobs(_))
  
  implicit val itemsWrites = new Writes[Jobs] {
    def writes(jobs: Jobs) = Json.obj(
      "items"  -> jobs.items
    )
  }
}
