package controllers

import models.{Category, CategoryRepository, Product, ProductRepository}
import play.api.data.Form
import play.api.data.Forms.{bigDecimal, longNumber, mapping, nonEmptyText, _}
import play.api.data.format.Formats._
import play.api.mvc._
import play.api.libs.json.{JsValue, Json}
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class CreateProductForm(name: String, description: String, categoryId: Long, price: BigDecimal, weight: Option[Double], height: Option[Double], width: Option[Double])
case class UpdateProductForm(id: Long, name: String, description: String, categoryId: Long, price: BigDecimal, weight: Option[Double], height: Option[Double], width: Option[Double])
object CreateProductForm {
  implicit val createProductFormat = Json.format[CreateProductForm]
}

@Singleton
class ProductController @Inject()(productsRepo: ProductRepository, categoryRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "categoryId" -> longNumber,
      "price" -> bigDecimal,
      "weight" -> optional(of(doubleFormat)),
      "height" -> optional(of(doubleFormat)),
      "width" -> optional(of(doubleFormat))
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  val updateProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "categoryId" -> longNumber,
      "price" -> bigDecimal,
      "weight" -> optional(of(doubleFormat)),
      "height" -> optional(of(doubleFormat)),
      "width" -> optional(of(doubleFormat))
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def addProductHandle = Action.async { implicit request =>
    var cate: Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete{
      case Success(cat) => cate = cat
      case Failure(_) => print("fail")
    }

    productForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.product_add(errorForm, cate))
        )
      },
      product => {
        productsRepo.create(product.name, product.description, product.categoryId, product.price, product.height, product.weight, product.width).map { _ =>
          Redirect(routes.ProductController.addProduct).flashing("success" -> "product created")
        }
      }
    )

  }

  def addProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepo.list()
    categories.map (cat => Ok(views.html.product_add(productForm, cat)))
  }

  def getProducts: Action[AnyContent] = Action.async { implicit request =>
    val produkty = productsRepo.list()
    produkty.map( products => Ok(views.html.products(products)))
  }

  def getProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteProduct(id: Long): Action[AnyContent] = Action {
    productsRepo.delete(id)
    Redirect("/products")
  }

  def updateProductHandle  = Action.async { implicit request =>
    var categ:Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete{
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    updateProductForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.product_update(errorForm, categ))
        )
      },
      product => {
        productsRepo.update(product.id, Product(product.id, product.name, product.description, product.categoryId, product.price, product.height, product.weight, product.width)).map { _ =>
          Redirect(routes.ProductController.updateProduct(product.id)).flashing("success" -> "product updated")
        }
      }
    )
  }

  def updateProduct(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var categ:Seq[Category] = Seq[Category]()
    val categories = categoryRepo.list().onComplete{
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    val produkt = productsRepo.getById(id)
    produkt.map(product => {
      val prodForm = updateProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.categoryId, product.price, product.height, product.weight, product.width))
      Ok(views.html.product_update(prodForm, categ))
    })
  }

  def getProductJson(id: Long) = Action.async {
    val produkty = productsRepo.getById(id)

    produkty.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  //json
  def getProductsJson = Action.async {
    //test
    val produkty = productsRepo.list()
//    val categories = categoryRepo.list()

//    val crossJoin = for {
//      (c, s) <- produkty join categories
//    } yield (c.categoryId, s.id)

//    produkty.leftJoin(categories).on { case (prod, cat) =>
//      prod.categoryId === cat.id
//    }.values.toList

//    produkty.flatMap(p =>
//      categories.filter(a => p.categoryId === a.id)
//        .map(a => (p.name, a.city))
//    ).result

    produkty.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def addProductJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateProductForm].map {
      product =>
        productsRepo.create(product.name, product.description, product.categoryId, product.price, product.height, product.weight, product.width).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateProductJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[Product].map {
      product =>
        productsRepo.updateJson(product.id, Product(product.id, product.name, product.description, product.categoryId, product.price, product.height, product.weight, product.width)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def deleteProductJson(id: Long): Action[AnyContent] = Action.async {
    productsRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

}