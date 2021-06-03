
package models

import play.api.libs.json.Json

case class UserAddress(id: Long, userId: Long, city: String, postalCode: String, country: String, telephone: String, mobile: String, addressLine: String)

object UserAddress {
  implicit val userAddressFormat = Json.format[UserAddress]
}