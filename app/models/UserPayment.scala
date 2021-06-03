package models

import play.api.libs.json.Json

import java.util.Date

case class UserPayment(id: Long, userId: Long, paymentType: String, provider: String, accountNumber: String, expiry: Date)

object UserPayment {
  implicit val userPaymentFormat = Json.format[UserPayment]
}