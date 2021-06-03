package controllers

import models.{Category, CategoryRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class CreateCategoryForm(name: String, description: String)
case class UpdateCategoryForm(id: Long, name: String, description: String)
object CreateCategoryForm {
  implicit val createCreateCategoryFormat = Json.format[CreateCategoryForm]
}

@Singleton
class CategoryController @Inject()(categoriesRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val categoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
    )(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  val updateCategoryForm: Form[UpdateCategoryForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
    )(UpdateCategoryForm.apply)(UpdateCategoryForm.unapply)
  }

  def addCategoryHandle = Action.async { implicit request =>
    categoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.category_add(errorForm))
        )
      },
      category => {
        categoriesRepo.create(category.name, category.description).map { _ =>
          Redirect(routes.CategoryController.addCategory).flashing("success" -> "category.created")
        }
      }
    )
  }

  def addCategory(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Future {
      Ok(views.html.category_add(categoryForm))
    }
  }

  def getCategories: Action[AnyContent] = Action.async { implicit request =>
    val categories = categoriesRepo.list()
    categories.map(el => Ok(views.html.categories(el)))
  }

  def getCategory(id: Long): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok("")
    }
  }

  def deleteCategory(id: Long): Action[AnyContent] = Action {
    categoriesRepo.delete(id)
    Redirect("/categories")
  }

  def updateCategoryHandle = Action.async { implicit request =>
    updateCategoryForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.category_update(errorForm))
        )
      },
      cat => {
        categoriesRepo.update(cat.id, Category(cat.id, cat.name, cat.description)).map { _ =>
          Redirect(routes.CategoryController.updateCategory(cat.id)).flashing("success" -> "category updated")
        }
      }
    )
  }

  def updateCategory(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val cat = categoriesRepo.getById(id)
    cat.map(category => {
      val form = updateCategoryForm.fill(UpdateCategoryForm(category.id, category.name, category.description))
      Ok(views.html.category_update(form))
    })
  }

  def getCategoriesJson = Action.async {
    val category = categoriesRepo.list()
    category.map { seq =>
      Ok(Json.toJson(seq))
    }
  }

  def getCategoryJson(id: Long) = Action.async {
      val category = categoriesRepo.getById(id)
      category.map { seq =>
        Ok(Json.toJson(seq))
      }
    }


  def addCategoryJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[CreateCategoryForm].map {
      category =>
        categoriesRepo.create(category.name, category.description).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def updateCategoryJson(): Action[JsValue] = Action.async(parse.json) { implicit request =>

    println(request.body)

    request.body.validate[Category].map {
      category =>
        categoriesRepo.updateJson(category.id, Category(category.id, category.name, category.description)).map { res =>
          Ok(Json.toJson(res))
        }
    }.getOrElse(Future.successful(BadRequest("incorrect data")))
  }

  def deleteCategoryJson(id: Long): Action[AnyContent] = Action.async {
    categoriesRepo.deleteJson(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

}