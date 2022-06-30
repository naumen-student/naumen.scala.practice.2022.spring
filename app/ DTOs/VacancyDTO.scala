package DTOs

import play.api.libs.json.{Json, Reads}

case class VacancyDTO(id: String,
                      name: Option[String],
                      alternate_url: Option[String],
                      snippet: Option[SnippetDTO],
                      salary: Option[SalaryDTO])

object VacancyDTO {
  implicit val vacancyDtoReader: Reads[VacancyDTO] = Json.reads[VacancyDTO]
}
