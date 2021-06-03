package controllers

import models.{User, UserAddress, UserAddressRepository, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UpdateUserAddressForm(id: Long, userId: Long, city: String, postalCode: String, country: String, telephone: String, mobile: String, addressLine: String);
case class CreateUserAddressForm(userId: Long, city: String, postalCode: String, country: String, telephone: String, mobile: String, addressLine: String);
object CreateUserAddressForm {
  implicit val createCreateUserAddressFormFormat = Json.format[CreateUserAddressForm]
}

@Singleton
class UserAddressController @Inject()(userAddressRepo: UserAddressRepository, userRepo: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createUserAddressForm: Form[CreateUserAddressForm] = Form {
    mapping(
      "userId" -> longNumber,
      "city" -> nonEmptyText,
      "postalCode" -> nonEmptyText,
      "country" -> nonEmptyText,
      "telephone" -> nonEmptyText,
      "mobile" -> nonEmptyText,
      "addressLine" -> nonEmptyText
    )(CreateUserAddressForm.apply)(CreateUserAddressForm.unapply)
  }

  val updateUserAddressForm: Form[UpdateUserAddressForm] = Form {
    mapping(
      "id" -> longNumber,
      "userId" -> longNumber,
      "city" -> nonEmptyText,
      "postalCode" -> nonEmptyText,
      "country" -> nonEmptyText,
      "telephone" -> nonEmptyText,
      "mobile" -> nonEmptyText,
      "addressLine" -> nonEmptyText
    )(UpdateUserAddressForm.apply)(UpdateUserAddressForm.unapply)
  }

  def addUserAddressHandle = Action.async { implicit request =>
    var user: Seq[User] = Seq[User]()
    val usersList = userRepo.list().onComplete{
      case Success(us) => user = us
      case Failure(_) => println("fail")
    }

    createUserAddressForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userAdress_add(errorForm, user))
        )
      },
      add => {
        userAddressRepo.create(add.userId, add.city, add.postalCode, add.country, add.telephone, add.mobile, add.addressLine).map { _ =>
          Redirect(routes.UserAddressController.addUserAddress).flashing("success" -> "userAddress created")
        }
      }
    )
  }

  def addUserAddress(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepo.list()
    user.map(u => Ok(views.html.userAdress_add(createUserAddressForm, u)))
  }

  def getUserAddresses: Action[AnyContent] = Action.async { implicit request =>
    val usersAdd = userAddressRepo.list()
    usersAdd.map(el => Ok(views.html.usersAddresses(el)))
  }

  def getUserAddress(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteUserAddress(id: Long): Action[AnyContent] = Action {
    userAddressRepo.delete(id)
    Redirect("/userAddresses")
  }

  def updateUserAddressHandle = Action.async { implicit request =>
    var user: Seq[User] = Seq[User]()
    val usersList = userRepo.list().onComplete{
      case Success(us) => user = us
      case Failure(_) => println("fail")
    }

    updateUserAddressForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userAddress_update(errorForm, user))
        )
      },
      add => {
        userAddressRepo.update(add.id, UserAddress(add.id, add.userId, add.city, add.postalCode, add.country, add.telephone, add.mobile, add.addressLine)).map { _ =>
          Redirect(routes.UserAddressController.updateUserAddress(add.id)).flashing("success" -> "userAddress updated")
        }
      }
    )
  }

  def updateUserAddress(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var userList: Seq[User] = Seq[User]()
    val usersListTest = userRepo.list().onComplete{
      case Success(us) => userList = us
      case Failure(_) => print("fail")
    }

    val usersAdd = userAddressRepo.getById(id)
    usersAdd.map(add => {
      val form = updateUserAddressForm.fill(UpdateUserAddressForm(add.id, add.userId, add.city, add.postalCode, add.country, add.telephone, add.mobile, add.addressLine))
      Ok(views.html.userAddress_update(form, userList))
    })
  }

  def getUsersAddressesJson = Action.async {
    val user = userAddressRepo.list()
    user.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getUserAddressesJson(id: Long) = Action.async {
    val user = userAddressRepo.getById(id)
    user.map { seq =>
      Ok(Json.toJson(seq))
    }
  }


  def adduserAddresJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateUserAddressForm].map {
      add =>
        userAddressRepo.create(add.userId, add.city, add.postalCode, add.country, add.telephone, add.mobile, add.addressLine).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateuserAddrestJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[UserAddress].map {
      add =>
        userAddressRepo.updateJson(add.id, UserAddress(add.id, add.userId, add.city, add.postalCode, add.country, add.telephone, add.mobile, add.addressLine)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def deleteuserAddresJson(id: Long): Action[AnyContent] = Action.async {
    userAddressRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

}

