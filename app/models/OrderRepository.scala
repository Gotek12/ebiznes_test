package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  implicit val JavaUtilDateMapper =
    MappedColumnType .base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))

  class OrderTable(tag: Tag) extends Table[Order](tag, "order") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("userId")
    def status = column[Int]("status")
    def items = column[Int]("items")
    def createdAt = column[Date]("createdAt")
    def tax = column[Float]("tax")
    def price = column[BigDecimal]("price")
    def shipping = column[Float]("shipping")
    def payed = column[Boolean]("payed")
    def realized = column[Boolean]("realized")
    def * = (id, userId, status, items, createdAt, tax, price, shipping, payed, realized) <> ((Order.apply _).tupled, Order.unapply)
  }

  val order = TableQuery[OrderTable]

  def list(): Future[Seq[Order]] = db.run {
    order.result
  }

  def delete(id: Long): Future[Unit] = db.run(order.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(order.filter(_.id === id).delete)

  def getById(id: Long): Future[Order] = db.run {
    order.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: Order): Future[Unit] = {
    val toUpdate: Order = newElement.copy(id)
    db.run(order.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: Order): Future[Int] = {
    val toUpdate: Order = newElement.copy(id)
    db.run(order.filter(_.id === id).update(toUpdate))
  }

  def create(userId: Long, status: Int, items: Int, createdAt: Date, tax: Float, price: BigDecimal, shipping: Float, payed: Boolean, realized: Boolean): Future[Order] = db.run {
    (order.map(el => (el.userId, el.status, el.items, el.createdAt, el.tax, el.price, el.shipping, el.payed, el.realized))
      returning order.map(_.id)
      into {case ((userId, status, items, createdAt, tax, price, shipping, payed, realized),id) => Order(id, userId, status, items, createdAt, tax, price, shipping, payed, realized)}
      ) += (userId, status, items, createdAt, tax, price, shipping, payed, realized)
  }
}
