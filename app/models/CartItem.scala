package models

import play.api.libs.json.Json

case class CartItem(id: Long, quantity: Int, productId: Long, cartId: Long)

object CartItem {
  implicit val cartItemFormat = Json.format[CartItem]
}
