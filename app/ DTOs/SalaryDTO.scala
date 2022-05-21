package DTOs

import play.api.libs.json.{Json, Reads}

case class SalaryDTO(currency: Option[String],
                     from: Option[Int],
                     gross: Option[Boolean],
                     to: Option[Int])

object SalaryDTO {
  implicit val salaryDtoReader: Reads[SalaryDTO] = Json.reads[SalaryDTO]
}
