package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import models.repository.UserRepository
import play.api.libs.json.Json
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class User2Controller @Inject()(userRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getUsersOauth(loginInfo: LoginInfo) = Action.async {
    val user = userRepo.retrieve(loginInfo)
    user.map { seq =>
      Ok(Json.toJson(seq))
    }
  }
}
