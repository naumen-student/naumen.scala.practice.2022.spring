package DTOs

import play.api.libs.json.{Json, Reads}

case class RegionDTO(id: String,
                     name: String,
                     areas: Seq[RegionDTO])

object RegionDTO {
  implicit val regionDtoReader: Reads[RegionDTO] = Json.reads[RegionDTO]
}