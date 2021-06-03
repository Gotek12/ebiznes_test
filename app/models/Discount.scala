package models

import play.api.libs.json.Json

import java.util.Date

case class Discount(id: Long, name: String, discountPercentage: Float, expire: Date, productId: Long)

object Discount {
  implicit val discountFormat = Json.format[Discount]
}