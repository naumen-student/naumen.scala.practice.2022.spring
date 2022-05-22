package model

import play.api.libs.json._

case class Area(id: String, name: String, areas: Option[Seq[Area]])

case class Child(children: Seq[Area])

object  Child {
  implicit val childReads: Reads[Child] = Json.reads[Child]
}

object Area {
  implicit val areaReads: Reads[Area] = Json.reads[Area]
}