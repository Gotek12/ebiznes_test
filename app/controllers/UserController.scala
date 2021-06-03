package controllers

import models.{User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, optional, sqlDate}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import java.sql.Date
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

case class CreateUserForm(nick: String, firstName: String, lastName: String, email: String, password: String, registeredAt: Option[Date], lastLogin: Option[Date])
case class UpdateUserForm(id: Long, nick: String, firstName: String, lastName: String, email: String, password: String, registeredAt: Option[Date], lastLogin: Option[Date])
object CreateUserForm {
  implicit val createCreateUserFormFormat = Json.format[CreateUserForm]
}

@Singleton
class UserController @Inject()(userRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val userForm: Form[CreateUserForm] = Form {
    mapping(
      "nick" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "registeredA" -> optional(sqlDate),
      "lastLogin" ->  optional(sqlDate)
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }

  val updateUserForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> longNumber,
      "nick" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "registeredA" -> optional(sqlDate),
      "lastLogin" -> optional(sqlDate)
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }

  def addUserHandle = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.user_add(errorForm))
        )
      },
      user => {
        userRepo.create(user.nick, user.firstName, user.lastName, user.email, user.password, user.registeredAt, user.lastLogin).map { _ =>
          Redirect(routes.UserController.addUser).flashing("success" -> "user.created")
        }
      }
    )
  }

  def addUser(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Future {
      Ok(views.html.user_add(userForm))
    }
  }

  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    val users = userRepo.list()
    users.map(el => Ok(views.html.users(el)))
  }

  def getUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteUser(id: Long): Action[AnyContent] = Action {
    userRepo.delete(id)
    Redirect("/users")
  }

  def updateUserHandle = Action.async { implicit request =>
    updateUserForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.user_update(errorForm))
        )
      },
      user => {
        userRepo.update(user.id, User(user.id, user.nick, user.firstName, user.lastName, user.email, user.password, null, null)).map { _ =>
          Redirect(routes.UserController.updateUser(user.id)).flashing("success" -> "user updated")
        }
      }
    )
  }

  def updateUser(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepo.getById(id)
    user.map(user => {
      val form = updateUserForm.fill(UpdateUserForm(user.id, user.nick, user.firstName, user.lastName, user.email, user.password, null, null))
      Ok(views.html.user_update(form))
    })
  }

  def getUsersJson = Action.async {
    val user = userRepo.list()
    user.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getUserJson(id: Long) = Action.async {
    val user = userRepo.getById(id)
    user.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addUserJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateUserForm].map {
      user =>
        userRepo.create(user.nick, user.firstName, user.lastName, user.email, user.password, user.registeredAt, user.lastLogin).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateUserJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[User].map {
      user =>
        userRepo.updateJson(user.id, User(user.id, user.nick, user.firstName, user.lastName, user.email, user.password, user.registeredAt, user.lastLogin)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def deleteUserJson(id: Long): Action[AnyContent] = Action.async {
    userRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }
}

