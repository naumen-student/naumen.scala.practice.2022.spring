package model

import java.util.UUID

case class Salary(id: UUID,
                  _from: Option[Int],
                  _to: Option[Int],
                  currency: Option[String],
                  gross: Option[Boolean])
