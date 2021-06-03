package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAddressRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class  UserAddressTable(tag: Tag) extends Table[UserAddress](tag, "userAddress") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("userId")
    def city = column[String]("city")
    def postalCode = column[String]("postalCode")
    def country = column[String]("country")
    def telephone = column[String]("telephone")
    def mobile = column[String]("mobile")
    def addressLine = column[String]("addressLine")

    def * = (id, userId, city, postalCode, country, telephone, mobile, addressLine) <> ((UserAddress.apply _).tupled, UserAddress.unapply)
  }

  val userAdd = TableQuery[UserAddressTable]

  def list(): Future[Seq[UserAddress]] = db.run {
    userAdd.result
  }

  def delete(id: Long): Future[Unit] = db.run(userAdd.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(userAdd.filter(_.id === id).delete)

  def getById(id: Long): Future[UserAddress] = db.run {
    userAdd.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: UserAddress): Future[Unit] = {
    val toUpdate: UserAddress = newElement.copy(id)
    db.run(userAdd.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: UserAddress): Future[Int] = {
    val toUpdate: UserAddress = newElement.copy(id)
    db.run(userAdd.filter(_.id === id).update(toUpdate))
  }

  def create(userId: Long, city: String, postalCode: String, country: String, telephone: String, mobile: String, addressLine: String): Future[UserAddress] = db.run {
    (userAdd.map(el => (el.userId, el.city, el.postalCode, el.country, el.telephone, el.mobile, el.addressLine))
      returning userAdd.map(_.id)
      into {case ((userId, city, postalCode, country, telephone, mobile, addressLine),id) => UserAddress(id, userId, city, postalCode, country, telephone, mobile, addressLine)}
      ) += (userId, city, postalCode, country, telephone, mobile, addressLine)
  }
}
