package DTOs

import play.api.libs.json.{Json, Reads}

case class SnippetDTO(requirement: Option[String],
                      responsibility: Option[String])

object SnippetDTO {
  implicit val snippetDtoReader: Reads[SnippetDTO] = Json.reads[SnippetDTO]
}
