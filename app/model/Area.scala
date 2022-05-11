package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Area(id: String, name: String, children: Option[JsValue])

object Area {
  implicit val areaReads: Reads[Area] = (
      (JsPath \ "id").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "areas").readNullable[JsValue]
    ) (Area.apply _)
}