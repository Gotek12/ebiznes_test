package models

import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import slick.driver.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "product") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def categoryId = column[Long]("categoryId")
    def price = column[BigDecimal]("price")
    def weight = column[Option[Double]]("weight")
    def height = column[Option[Double]]("height")
    def width = column[Option[Double]]("width")
    def * = (id, name, description, categoryId, price, weight, height, width) <> ((Product.apply _).tupled, Product.unapply)
  }

  val product = TableQuery[ProductTable]

  def list(): Future[Seq[Product]] = db.run {
    product.result
  }

  def delete(id: Long): Future[Unit] = db.run(product.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(product.filter(_.id === id).delete)

  def getById(id: Long): Future[Product] = db.run {
    product.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: Product): Future[Unit] = {
    val toUpdate: Product = newElement.copy(id)
    db.run(product.filter(_.id === id).update(toUpdate)).map(_ => ())
  }

  def updateJson(id: Long, newElement: Product): Future[Int] = {
    val toUpdate: Product = newElement.copy(id)
    db.run(product.filter(_.id === id).update(toUpdate))
  }

  def create(name: String, description: String, categoryId: Long, price: BigDecimal, weight: Option[Double], height: Option[Double], width: Option[Double]): Future[Product] = db.run {
    (product.map(el => (el.name, el.description, el.categoryId, el.price, el.weight, el.height, el.width))
      returning product.map(_.id)
      into {case ((name, description, categoryId, price, weight, height, width),id) => Product(id, name, description, categoryId, price, weight, height, width)}
      ) += (name, description, categoryId, price, weight, height, width)
  }
}
