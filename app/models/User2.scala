package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.{Json, OFormat}

case class User2(id: Long, loginInfo: LoginInfo, email: String) extends Identity

object User2 {
  implicit val loginInfoFormat: OFormat[LoginInfo] = Json.format[LoginInfo]
  implicit val userFormat: OFormat[User2] = Json.format[User2]
}