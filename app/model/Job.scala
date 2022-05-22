package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.language.implicitConversions


case class Job(id: String,
                  title: String,
                  requirements: Option[String],
                  responsibility: Option[String],
                  salaryFrom: Option[Int],
                  salaryTo: Option[Int],
                  salaryCurrency: Option[String],
                  url: String)

case class JobDTO(id: String,
               title: String,
               requirements: Option[String],
               responsibility: Option[String],
               salaryFrom: Option[Int],
               salaryTo: Option[Int],
               salaryCurrency: Option[String],
               url: String)

object Job {
  implicit  def jobToJobDTO(job: Job): JobDTO = JobDTO(
    job.id,
    job.title,
    job.requirements,
    job.responsibility,
    job.salaryFrom,
    job.salaryTo,
    job.salaryCurrency,
    job.url
  )
}

object JobDTO {
  implicit def jobDTOtoJob(jobDTO: JobDTO): Job = Job(
    jobDTO.id,
    jobDTO.title,
    jobDTO.requirements,
    jobDTO.responsibility,
    jobDTO.salaryFrom,
    jobDTO.salaryTo,
    jobDTO.salaryCurrency,
    jobDTO.url
  )

  implicit  val jobWrites: Writes[JobDTO] = Json.writes[JobDTO]

  implicit val jobReads: Reads[JobDTO] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \\ "requirement").readNullable[String] and
      (JsPath \\ "responsibility").readNullable[String] and
      (JsPath \ "salary" \ "from").readNullable[Int] and
      (JsPath \ "salary" \ "to").readNullable[Int] and
      (JsPath \ "salary" \ "currency").readNullable[String] and
      (JsPath \ "alternate_url").read[String]
    )(JobDTO.apply _)
}
