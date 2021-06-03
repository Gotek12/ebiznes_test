package controllers

import models.{Cart, CartRepository, Product, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{date, longNumber, mapping, of}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import java.util.Date
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UpdateCartForm(id: Long, userId: Long)
case class CreateCartForm(userId: Long)
object CreateCartForm {
  implicit val createCreateCartFormat = Json.format[CreateCartForm]
}

@Singleton
class CartController @Inject()(cartsRepo: CartRepository, userRepository: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createCartForm: Form[CreateCartForm] = Form {
    mapping(
      "userId" -> longNumber
    )(CreateCartForm.apply)(CreateCartForm.unapply)
  }

  val updateCartForm: Form[UpdateCartForm] = Form {
    mapping(
      "id" -> longNumber,
      "userId" -> longNumber
    )(UpdateCartForm.apply)(UpdateCartForm.unapply)
  }

  def addCartHandle() = Action.async { implicit request =>
    var user: Seq[User] = Seq[User]()
    val usersList = userRepository.list().onComplete{
      case Success(us) => user = us
      case Failure(_) => println("fail")
    }

    createCartForm.bindFromRequest.fold(
      errorForm => {
        println("error")
        Future.successful(
          BadRequest(views.html.cart_add(errorForm, user))
        )
      },
      c => {
        cartsRepo.create(c.userId, new Date()).map { _ =>
          Redirect(routes.CartController.addCart).flashing("success" -> "cart created")
        }
      }
    )
  }

  def addCart(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepository.list()
    user.map(u => Ok(views.html.cart_add(createCartForm, u)))
  }

  def getCart(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val cart = cartsRepo.getById(id)
    Future {
      Ok("")
    }
  }

  def getCarts = Action.async { implicit request =>
    val cart = cartsRepo.list()
    cart.map(el => Ok(views.html.cart(el)))
  }

  def deleteCart(id: Long): Action[AnyContent] = Action {
    cartsRepo.delete(id)
    Redirect("/carts")
  }

  def updateCartHandle() = Action.async { implicit request =>
    var userek: Seq[User] = Seq[User]()
    val prodList = userRepository.list().onComplete {
      case Success(u) => userek = u
      case Failure(_) => println("fail")
    }

    updateCartForm.bindFromRequest.fold(
      errorForm => {
        println("error")
        Future.successful(
          BadRequest(views.html.cart_update(errorForm, userek))
        )
      },
      c => {
        cartsRepo.update(c.id, Cart(c.id, c.userId, new Date())).map { _ =>
          Redirect(routes.CartController.updateCart(c.id)).flashing("success" -> "cart update")
        }
      }
    )
  }

  def updateCart(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var userList: Seq[User] = Seq[User]()
    val usersListTest = userRepository.list().onComplete{
      case Success(us) => userList = us
      case Failure(_) => print("fail")
    }

    var cart = cartsRepo.getById(id)
    cart.map(c => {
        val form = updateCartForm.fill(UpdateCartForm(c.id, c.userId))
        Ok(views.html.cart_update(form, userList))
    })
  }

  def getCartsJson = Action.async {
    val cart = cartsRepo.list()
    cart.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getCartJson(id: Long) = Action.async {
    val cart = cartsRepo.getById(id)
    cart.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addCartJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateCartForm].map {
      c =>
        cartsRepo.create(c.userId, new Date()).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateCartJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[Cart].map {
      c =>
        cartsRepo.updateJson(c.id, Cart(c.id, c.userId, new Date())).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def deleteCartJson(id: Long): Action[AnyContent] = Action.async {
    cartsRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }
}
