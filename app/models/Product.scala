package models

import play.api.libs.json.Json

case class Product(id: Long, name: String, description: String, categoryId: Long, price: BigDecimal, weight: Option[Double], height: Option[Double], width: Option[Double])

object Product {
  implicit val productFormat = Json.format[Product]
}
