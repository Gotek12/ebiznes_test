package controllers

import models.{User, UserPayment, UserPaymentRepository, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{date, longNumber, mapping, nonEmptyText, optional, sqlDate}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import java.sql.Date
import java.text.SimpleDateFormat
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.runtime.universe.Try
import scala.util.{Failure, Success}


case class UpdateUserPaymentForm(id: Long, userId: Long, paymentType: String, provider: String, accountNumber: String, expiry: java.util.Date)
case class CreateUserPaymentForm(userId: Long, paymentType: String, provider: String, accountNumber: String, expiry: java.util.Date)
object CreateUserPaymentForm {
  implicit val createCreateUserPaymentFormFormat = Json.format[CreateUserPaymentForm]
}

@Singleton
class UserPaymentController @Inject()(userPaymentRepo: UserPaymentRepository, userRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createUserPaymentForm: Form[CreateUserPaymentForm] = Form {
    mapping(
      "userId" -> longNumber,
      "paymentType" -> nonEmptyText,
      "provider" -> nonEmptyText,
      "accountNumber" -> nonEmptyText,
      "expiry" -> date,
    )(CreateUserPaymentForm.apply)(CreateUserPaymentForm.unapply)
  }

  val updateUserPaymentForm: Form[UpdateUserPaymentForm] = Form {
    mapping(
      "id" -> longNumber,
      "userId" -> longNumber,
      "paymentType" -> nonEmptyText,
      "provider" -> nonEmptyText,
      "accountNumber" -> nonEmptyText,
      "expiry" -> date,
    )(UpdateUserPaymentForm.apply)(UpdateUserPaymentForm.unapply)
  }

  def addUserPaymentHandle = Action.async { implicit request =>
    var user: Seq[User] = Seq[User]()
    val usersList = userRepo.list().onComplete{
      case Success(us) => user = us
      case Failure(_) => println("fail")
    }

    createUserPaymentForm.bindFromRequest.fold(
      errorForm => {
        println("error")
        Future.successful(
          BadRequest(views.html.userPayment_add(errorForm, user))
        )
      },
      user => {
        userPaymentRepo.create(user.userId, user.paymentType, user.provider, user.accountNumber, user.expiry).map { _ =>
          Redirect(routes.UserPaymentController.addUserPayment).flashing("success" -> "userPayment.created")
        }
      }
    )
  }

  def addUserPayment(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepo.list()
    user.map(u => Ok(views.html.userPayment_add(createUserPaymentForm, u)))
  }

  def getUserPayments: Action[AnyContent] = Action.async { implicit request =>
    val usersPayments = userPaymentRepo.list()
    usersPayments.map(el => Ok(views.html.usersPayments(el)))
  }

  def getUserPayment(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteUserPayment(id: Long): Action[AnyContent] = Action {
    userPaymentRepo.delete(id)
    Redirect("/userPayments")
  }

  def updateUserPaymentHandle = Action.async { implicit request =>
    var user: Seq[User] = Seq[User]()
    val usersList = userRepo.list().onComplete{
      case Success(us) => user = us
      case Failure(_) => print("fail")
    }
    updateUserPaymentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userPayment_update(errorForm, user))
        )
      },
      user => {
        userPaymentRepo.update(user.id, UserPayment(user.id, user.userId, user.paymentType, user.provider, user.accountNumber, user.expiry)).map { _ =>
          Redirect(routes.UserPaymentController.updateUserPayment(user.id)).flashing("success" -> "userPayment updated")
        }
      }
    )
  }

  def updateUserPayment(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var userList: Seq[User] = Seq[User]()
    val usersListTest = userRepo.list().onComplete{
      case Success(us) => userList = us
      case Failure(_) => print("fail")
    }

    val usersPayments = userPaymentRepo.getById(id)
    usersPayments.map(user => {
      val form = updateUserPaymentForm.fill(UpdateUserPaymentForm(user.id, user.userId, user.paymentType, user.provider, user.accountNumber, user.expiry))
      Ok(views.html.userPayment_update(form, userList))
    })
  }

  def getUserPaymentsJson = Action.async {
    val user = userPaymentRepo.list()
    user.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getUserPaymentJson(id: Long) = Action.async {
    val user = userPaymentRepo.getById(id)
    user.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addUserPaymentJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateUserPaymentForm].map {
      user =>
        userPaymentRepo.create(user.userId, user.paymentType, user.provider, user.accountNumber, user.expiry).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateUserPaymentJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[UserPayment].map {
      user =>
        userPaymentRepo.updateJson(user.id, UserPayment(user.id, user.userId, user.paymentType, user.provider, user.accountNumber, user.expiry)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))

  }


  def deleteUserPaymentJson(id: Long): Action[AnyContent] = Action.async {
    userPaymentRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

}
