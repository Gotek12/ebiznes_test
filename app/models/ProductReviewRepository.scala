package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductReviewRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  implicit val JavaUtilDateMapper =
    MappedColumnType .base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))

  class ProductReviewTable(tag: Tag) extends Table[ProductReview](tag, "productReview") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def productId = column[Long]("productId")
    def date = column[Date]("date")
    def description = column[String]("description")
    def userId = column[Long]("userId")
    def * = (id, productId, date, description, userId) <> ((ProductReview.apply _).tupled, ProductReview.unapply)
  }

  val productRev = TableQuery[ProductReviewTable]

  def list(): Future[Seq[ProductReview]] = db.run {
    productRev.result
  }

  def delete(id: Long): Future[Unit] = db.run(productRev.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(productRev.filter(_.id === id).delete)

  def getById(id: Long): Future[ProductReview] = db.run {
    productRev.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: ProductReview): Future[Unit] = {
    val toUpdate: ProductReview = newElement.copy(id)
    db.run(productRev.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: ProductReview): Future[Int] = {
    val toUpdate: ProductReview = newElement.copy(id)
    db.run(productRev.filter(_.id === id).update(toUpdate))
  }

  def create(productId: Long, date: Date, description: String, userId: Long): Future[ProductReview] = db.run {
    (productRev.map(el => (el.productId, el.date, el.description, el.userId))
      returning productRev.map(_.id)
      into {case ((productId, date, description, userId),id) => ProductReview(id, productId, date, description, userId)}
      ) += (productId, date, description, userId)
  }
}
