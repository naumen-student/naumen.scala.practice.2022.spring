package model

import play.api.libs.json._

case class Area(id: String, name: String, areas: Option[Seq[Area]])

object Area {
  implicit val areaReads: Reads[Area] = Json.reads[Area]
}