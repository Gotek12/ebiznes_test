package controllers

import models.{Cart, CartItem, CartItemRepository, CartRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms.{date, longNumber, mapping, nonEmptyText, number}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results.BadRequest
import play.api.mvc._

import javax.inject._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UpdateCartItemForm(id: Long, quantity: Int, productId: Long, cartId: Long)
case class CreateCartItemForm(quantity: Int, productId: Long, cartId: Long)
object CreateCartItemForm {
  implicit val createCreateCartItemFormat = Json.format[CreateCartItemForm]
}

@Singleton
class CartItemController @Inject()(cartItemsRepo: CartItemRepository, cartRepository: CartRepository, productRepository: ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createCartItemForm: Form[CreateCartItemForm] = Form {
    mapping(
      "quantity" -> number,
      "productId" -> longNumber,
      "cardId" -> longNumber
    )(CreateCartItemForm.apply)(CreateCartItemForm.unapply)
  }

  val updateCartItemForm: Form[UpdateCartItemForm] = Form {
    mapping(
      "id" -> longNumber,
      "quantity" -> number,
      "productId" -> longNumber,
      "cardId" -> longNumber
    )(UpdateCartItemForm.apply)(UpdateCartItemForm.unapply)
  }

  def addCartItemHandle = Action.async { implicit request =>

    var carSeq: Seq[Cart] = Seq[Cart]()
    var prodSeq: Seq[Product] = Seq[Product]()


    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    cartRepository.list().onComplete{
      case Success(u) => carSeq = u
      case Failure(_) => println("fail")
    }

    Thread.sleep(10)

    createCartItemForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.cartItem_add(errorForm, prodSeq, carSeq))
        )
      },
      add => {
        cartItemsRepo.create(add.quantity, add.productId, add.cartId).map { _ =>
          Redirect(routes.CartItemController.addCartItem).flashing("success" -> "cartItem created")
        }
      }
    )
  }

  def addCartItem: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var prodSeq: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    val carSeq = cartRepository.list()
    carSeq.map(u => Ok(views.html.cartItem_add(createCartItemForm, prodSeq, u)))
  }

  def getCartItem(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def getCartItems: Action[AnyContent] = Action.async { implicit request =>
    val cartIt = cartItemsRepo.list()
    cartIt.map(el => Ok(views.html.cartItems(el)))
  }

  def deleteCartItem(id: Long): Action[AnyContent] = Action {
    cartItemsRepo.delete(id)
    Redirect("/cartItems")
  }

  def updateCartItemHandle() = Action.async { implicit request =>
    var prodSeq: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    var carSeq: Seq[Cart] = Seq[Cart]()
    cartRepository.list().onComplete{
      case Success(us) => carSeq = us
      case Failure(_) => println("fail")
    }

    updateCartItemForm.bindFromRequest.fold(
      errorForm => {
        Future(
          BadRequest(views.html.cartItem_update(errorForm, prodSeq, carSeq))
        )
      },
      add => {
        cartItemsRepo.update(add.id, CartItem(add.id, add.quantity, add.productId, add.cartId)).map { _ =>
          Redirect(routes.CartItemController.updateCartItem(add.id)).flashing("success" -> "cartItem update")
        }
      }
    )
  }

  def updateCartItem(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var prodSeq: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    var carSeq: Seq[Cart] = Seq[Cart]()
    cartRepository.list().onComplete{
      case Success(us) => carSeq = us
      case Failure(_) => println("fail")
    }

    var cartIt = cartItemsRepo.getById(id)
    cartIt .map(e => {
      val form = updateCartItemForm.fill(UpdateCartItemForm(e.id, e.quantity, e.productId, e.cartId))
      Ok(views.html.cartItem_update(form, prodSeq, carSeq))
    })

  }

  def getCartsItemsJson = Action.async {
    val cart = cartItemsRepo.list()
    cart.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getCarItemsJson(id: Long) = Action.async {
    val cart = cartItemsRepo.getById(id)
    cart.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addCarItemJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateCartItemForm].map {
      add =>
        cartItemsRepo.create(add.quantity, add.productId, add.cartId).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateCarItemJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[CartItem].map {
      add =>
        cartItemsRepo.updateJson(add.id, CartItem(add.id, add.quantity, add.productId, add.cartId)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }


  def deleteCarItemJson(id: Long): Action[AnyContent] = Action.async {
    cartItemsRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }
}
