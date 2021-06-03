package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartItemRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CartItemTable(tag: Tag) extends Table[CartItem](tag, "cartItem") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def quantity = column[Int]("quantity")
    def productId = column[Long]("productId")
    def cartId = column[Long]("cartId")
    def * = (id, quantity, productId, cartId) <> ((CartItem.apply _).tupled, CartItem.unapply)
  }

  val cartItem = TableQuery[CartItemTable]

  def list(): Future[Seq[CartItem]] = db.run {
    cartItem.result
  }

  def delete(id: Long): Future[Unit] = db.run(cartItem.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(cartItem.filter(_.id === id).delete)

  def getById(id: Long): Future[CartItem] = db.run {
    cartItem.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: CartItem): Future[Unit] = {
    val toUpdate: CartItem = newElement.copy(id)
    db.run(cartItem.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: CartItem): Future[Int] = {
    val toUpdate: CartItem = newElement.copy(id)
    db.run(cartItem.filter(_.id === id).update(toUpdate))
  }

  def create(quantity: Int, productId: Long, cartId: Long): Future[CartItem] = db.run {
    (cartItem.map(el => (el.quantity, el.productId, el.cartId))
      returning cartItem.map(_.id)
      into {case ((quantity, productId, cartId),id) => CartItem(id, quantity, productId, cartId)}
      ) += (quantity, productId, cartId)
  }
}
