package models

import play.api.libs.json.Json

import java.util.Date

case class ProductReview(id: Long, productId: Long, date: Date, description: String, userId: Long)

object ProductReview {
  implicit val productReviewFormat = Json.format[ProductReview]
}