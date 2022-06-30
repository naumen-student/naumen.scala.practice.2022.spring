package DTOs

import play.api.libs.json.{Json, Reads}

case class HolderDTO(pages: Int,
                     items: Option[Seq[VacancyDTO]])

object HolderDTO {
  implicit val holderDtoReader: Reads[HolderDTO] = Json.reads[HolderDTO]
}
