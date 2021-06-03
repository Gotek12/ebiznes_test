package controllers

import models.{Discount, DiscountRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms.{date, longNumber, mapping, nonEmptyText, of}
import play.api.data.format.Formats.floatFormat
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import java.util.Date
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UpdateDiscountForm(id: Long, name: String, discountPercentage: Float, expire: Date, productId: Long)
case class CreateDiscountForm(name: String, discountPercentage: Float, expire: Date, productId: Long)
object CreateDiscountForm {
  implicit val createCreateDiscountFormat = Json.format[CreateDiscountForm]
}

@Singleton
class DiscountController @Inject()(discountRepo: DiscountRepository, productRepository: ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createDiscountForm: Form[CreateDiscountForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "discountPercentage" -> of(floatFormat),
      "expire" -> date,
      "productId" -> longNumber
    )(CreateDiscountForm.apply)(CreateDiscountForm.unapply)
  }

  val updateDiscountForm: Form[UpdateDiscountForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "discountPercentage" -> of(floatFormat),
      "expire" -> date,
      "productId" -> longNumber
    )(UpdateDiscountForm.apply)(UpdateDiscountForm.unapply)
  }

  def addDiscountHandle() = Action.async { implicit request =>
    var prod: Seq[Product] = Seq[Product]()
    val prodList = productRepository.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => println("fail")
    }

    createDiscountForm.bindFromRequest.fold(
      errorForm => {
        println("error")
        Future.successful(
          BadRequest(views.html.discount_add(errorForm, prod))
        )
      },
      dis => {
        discountRepo.create(dis.name, dis.discountPercentage, dis.expire, dis.productId).map { _ =>
          Redirect(routes.DiscountController.addDiscount).flashing("success" -> "discount created")
        }
      }
    )
  }

  def addDiscount() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val prod = productRepository.list()
    prod.map(u => Ok(views.html.discount_add(createDiscountForm, u)))
  }

  def getDiscounts: Action[AnyContent] = Action.async { implicit request =>
    val dis = discountRepo.list()
    dis.map(el => Ok(views.html.discounts(el)))
  }

  def getDiscount(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteDiscount(id: Long): Action[AnyContent] = Action {
    discountRepo.delete(id)
    Redirect("/discounts")
  }

  def updateDiscountHandle() = Action.async { implicit request =>
    var prod: Seq[Product] = Seq[Product]()
    val prodList = productRepository.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => println("fail")
    }

    updateDiscountForm.bindFromRequest.fold(
      errorForm => {
        println("error")
        Future.successful(
          BadRequest(views.html.discount_update(errorForm, prod))
        )
      },
      dis => {
        discountRepo.update(dis.id, Discount(dis.id, dis.name, dis.discountPercentage, dis.expire, dis.productId)).map { _ =>
          Redirect(routes.DiscountController.updateDiscount(dis.id)).flashing("success" -> "discount created")
        }
      }
    )
  }

  def updateDiscount(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var prod: Seq[Product] = Seq[Product]()
    val prodList = productRepository.list().onComplete {
      case Success(pr) => prod = pr
      case Failure(_) => println("fail")
    }

    val discounts = discountRepo.getById(id)
    discounts.map(dis => {
      val form = updateDiscountForm.fill(UpdateDiscountForm(dis.id, dis.name, dis.discountPercentage, dis.expire, dis.productId))
      Ok(views.html.discount_update(form, prod))
    })
  }

  def getDiscountsJson = Action.async {
    val dis = discountRepo.list()
    dis.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getDiscountJson(id: Long)  = Action.async {
    val dis = discountRepo.getById(id)
    dis.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addDiscountJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateDiscountForm].map {
      dis =>
        discountRepo.create(dis.name, dis.discountPercentage, dis.expire, dis.productId).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateDiscountJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[Discount].map {
      dis =>
        discountRepo.updateJson(dis.id, Discount(dis.id,dis.name, dis.discountPercentage, dis.expire, dis.productId)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def deleteDiscountJson(id: Long): Action[AnyContent] = Action.async {
    discountRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

}