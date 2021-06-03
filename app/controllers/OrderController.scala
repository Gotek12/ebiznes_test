package controllers

import models.{Order, OrderRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{bigDecimal, boolean, date, longNumber, mapping, number, of}
import play.api.data.format.Formats.{floatFormat, intFormat}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, _}

import java.util.Date
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class CreateOrderForm(userId: Long, status: Int, items: Int, tax: Float, price: BigDecimal, shipping: Float, payed: Boolean, realized: Boolean)
case class UpdateOrderForm(id: Long, userId: Long, status: Int, items: Int, tax: Float, price: BigDecimal, shipping: Float, payed: Boolean, realized: Boolean)
object CreateOrderForm {
  implicit val createCreateOrderFormFormat = Json.format[CreateOrderForm]
}

@Singleton
class OrderController @Inject()(orderRepo: OrderRepository, userRepository: UserRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createOrderForm: Form[CreateOrderForm] = Form {
    mapping(
      "userId" -> longNumber,
      "status" -> number,
      "items" -> number,
      "tax" -> of(floatFormat),
      "price" -> bigDecimal,
      "shipping" -> of(floatFormat),
      "payed" -> boolean,
      "realized" -> boolean
    )(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }

  val updateOrderForm: Form[UpdateOrderForm] = Form {
    mapping(
      "id" -> longNumber,
      "userId" -> longNumber,
      "status" -> number,
      "items" -> number,
      "tax" -> of(floatFormat),
      "price" -> bigDecimal,
      "shipping" -> of(floatFormat),
      "payed" -> boolean,
      "realized" -> boolean
    )(UpdateOrderForm.apply)(UpdateOrderForm.unapply)

  }

  def addOrderHandle = Action.async { implicit request =>
    var user: Seq[User] = Seq[User]()
    val usersList = userRepository.list().onComplete {
      case Success(us) => user = us
      case Failure(_) => println("fail")
    }

    createOrderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.order_add(errorForm, user))
        )
      },
      add => {
        orderRepo.create(add.userId, add.status, add.items, new Date(), add.tax, add.price, add.shipping, add.payed, add.realized).map { _ =>
          Redirect(routes.OrderController.addOrder).flashing("success" -> "order created")
        }
      }
    )
  }

  def addOrder(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepository.list()
    user.map(u => Ok(views.html.order_add(createOrderForm, u)))
  }

  def getOrders: Action[AnyContent] = Action.async { implicit request =>
    var ordersList = orderRepo.list()
    ordersList.map(el => Ok(views.html.orders(el)))
  }

  def getOrder(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteOrder(id: Long): Action[AnyContent] = Action {
    orderRepo.delete(id)
    Redirect("/orders")
  }

  def updateOrderHandle = Action.async { implicit request =>
    var user: Seq[User] = Seq[User]()
    val usersList = userRepository.list().onComplete {
      case Success(us) => user = us
      case Failure(_) => println("fail")
    }

    updateOrderForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.order_update(errorForm, user))
        )
      },
      add => {
        orderRepo.update(add.id, Order(add.id, add.userId, add.status, add.items, new Date(), add.tax, add.price, add.shipping, add.payed, add.realized)).map { _ =>
          Redirect(routes.OrderController.updateOrder(add.id)).flashing("success" -> "order update")
        }
      }
    )
  }

  def updateOrder(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var userList: Seq[User] = Seq[User]()
    val usersListTest = userRepository.list().onComplete {
      case Success(us) => userList = us
      case Failure(_) => print("fail")
    }

    val ordersRepoAdd = orderRepo.getById(id)
    ordersRepoAdd.map(add => {
      val form = updateOrderForm.fill(UpdateOrderForm(add.id, add.userId, add.status, add.items, add.tax, add.price, add.shipping, add.payed, add.realized))
      Ok(views.html.order_update(form, userList))
    })
  }

  def getOrdersJson = Action.async {
    val order = orderRepo.list()
    order.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getOrderJson(id: Long) = Action.async {
    val order = orderRepo.getById(id)
    order.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addOrderJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateOrderForm].map {
      add =>
        orderRepo.create(add.userId, add.status, add.items, new Date(), add.tax, add.price, add.shipping, add.payed, add.realized).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateOrderJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[Order].map {
      add =>
        orderRepo.updateJson(add.id, Order(add.id, add.userId, add.status, add.items, new Date(), add.tax, add.price, add.shipping, add.payed, add.realized)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }


  def deleteOrderJson(id: Long): Action[AnyContent] = Action.async {
    orderRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

}