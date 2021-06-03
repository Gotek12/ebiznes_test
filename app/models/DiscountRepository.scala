package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiscountRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  implicit val JavaUtilDateMapper =
    MappedColumnType .base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))

  class DiscountTable(tag: Tag) extends Table[Discount](tag, "discount") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def discountPercentage = column[Float]("discountPercentage")
    def expire = column[Date]("expire")
    def productId = column[Long]("productId")
    def * = (id, name, discountPercentage, expire, productId) <> ((Discount.apply _).tupled, Discount.unapply)
  }

  val discount = TableQuery[DiscountTable]

  def list(): Future[Seq[Discount]] = db.run {
    discount.result
  }

  def delete(id: Long): Future[Unit] = db.run(discount.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(discount.filter(_.id === id).delete)

  def getById(id: Long): Future[Discount] = db.run {
    discount.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: Discount): Future[Unit] = {
    val toUpdate: Discount = newElement.copy(id)
    db.run(discount.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: Discount): Future[Int] = {
    val toUpdate: Discount = newElement.copy(id)
    db.run(discount.filter(_.id === id).update(toUpdate))
  }

  def create(name: String, discountPercentage: Float, expire: Date, productId: Long): Future[Discount] = db.run {
    (discount.map(el => (el.name, el.discountPercentage, el.expire, el.productId))
      returning discount.map(_.id)
      into {case ((name, discountPercentage, expire, productId),id) => Discount(id, name, discountPercentage, expire, productId)}
      ) += (name, discountPercentage, expire, productId)
  }
}
