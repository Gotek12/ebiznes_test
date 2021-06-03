package controllers

import models.{Cart, Product, ProductRepository, ProductReview, ProductReviewRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{date, longNumber, mapping, nonEmptyText, number}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import java.util.Date
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UpdateProductReviewForm(id: Long, productId: Long, date: Date, description: String, userId: Long)
case class CreateProductReviewForm(productId: Long, date: Date, description: String, userId: Long)
object CreateProductReviewForm {
  implicit val createCreateProductReviewFormFormat = Json.format[CreateProductReviewForm]
}

@Singleton
class ProductReviewController @Inject()(productReviewRepo: ProductReviewRepository, userRepository: UserRepository, productRepository: ProductRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createProductReviewForm: Form[CreateProductReviewForm] = Form {
    mapping(
      "productId" -> longNumber,
      "date" -> date,
      "description" -> nonEmptyText,
      "userId" -> longNumber
    )(CreateProductReviewForm.apply)(CreateProductReviewForm.unapply)
  }

  val updateProductReviewForm: Form[UpdateProductReviewForm] = Form {
    mapping(
      "id" -> longNumber,
      "productId" -> longNumber,
      "date" -> date,
      "description" -> nonEmptyText,
      "userId" -> longNumber
    )(UpdateProductReviewForm.apply)(UpdateProductReviewForm.unapply)
  }

  def addProductReviewHandle = Action.async { implicit request =>
    var userSeq: Seq[User] = Seq[User]()
    var prodSeq: Seq[Product] = Seq[Product]()


    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    userRepository.list().onComplete{
      case Success(u) => userSeq = u
      case Failure(_) => println("fail")
    }

    Thread.sleep(10)

    createProductReviewForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productReview_add(errorForm, prodSeq, userSeq))
        )
      },
      add => {
        productReviewRepo.create(add.productId, add.date, add.description, add.userId).map { _ =>
          Redirect(routes.ProductReviewController.addProductReview).flashing("success" -> "review created")
        }
      }
    )
  }

  def addProductReview: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var prodSeq: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    val userSeq = userRepository.list()
    userSeq.map(u => Ok(views.html.productReview_add(createProductReviewForm, prodSeq, u)))
  }

  def getProductReviews: Action[AnyContent] = Action.async { implicit request =>
    val rew = productReviewRepo.list()
    rew.map(el => Ok(views.html.productReview(el)))
  }

  def getProductReview(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteProductReview(id: Long): Action[AnyContent] = Action {
    productReviewRepo.delete(id)
    Redirect("/productReviews")
  }

  def updateProductReviewHandle() = Action.async { implicit request =>
    var userSeq: Seq[User] = Seq[User]()
    var prodSeq: Seq[Product] = Seq[Product]()

    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    userRepository.list().onComplete{
      case Success(u) => userSeq = u
      case Failure(_) => println("fail")
    }

    updateProductReviewForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productReview_update(errorForm, prodSeq, userSeq))
        )
      },
      add => {
        productReviewRepo.update(add.id, ProductReview(add.id, add.productId, add.date, add.description, add.userId)).map { _ =>
          Redirect(routes.ProductReviewController.updateProductReview(add.id)).flashing("success" -> "review update")
        }
      }
    )
  }

  def updateProductReview(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var userSeq: Seq[User] = Seq[User]()
    var prodSeq: Seq[Product] = Seq[Product]()


    productRepository.list().onComplete{
      case Success(us) => prodSeq = us
      case Failure(_) => println("fail")
    }

    userRepository.list().onComplete{
      case Success(u) => userSeq = u
      case Failure(_) => println("fail")
    }

    var productR = productReviewRepo.getById(id)
    productR.map(e => {
      val form = updateProductReviewForm.fill(UpdateProductReviewForm(e.id, e.productId, e.date, e.description, e.userId))
      Ok(views.html.productReview_update(form, prodSeq, userSeq))
    })
  }

  def getProductdReviewsJson = Action.async {
    val prod = productReviewRepo.list()
    prod.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getProductReviewsJson(id: Long) = Action.async {
    val prod = productReviewRepo.getById(id)
    prod.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addProductReviewJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateProductReviewForm].map {
      add =>
        productReviewRepo.create(add.productId, add.date, add.description, add.userId).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateProductReviewJson(): Action[JsValue] = Action.async(parse.json) { request =>

    println(request.body)

    request.body.validate[ProductReview].map {
      add =>
        productReviewRepo.updateJson(add.id, ProductReview(add.id, add.productId, add.date, add.description, add.userId)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def deleteProductReviewJson(id: Long): Action[AnyContent] = Action.async {
    productReviewRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

}