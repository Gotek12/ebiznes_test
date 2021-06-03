package models

import play.api.libs.json.Json

import java.util.Date

case class Cart(id: Long, userId: Long, createdAt: Date)

object Cart {
  implicit val cartFormat = Json.format[Cart]
}