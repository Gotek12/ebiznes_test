package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.lifted.MappedToBase.mappedToIsomorphism

import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserPaymentRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  implicit val JavaUtilDateMapper =
    MappedColumnType .base[java.util.Date, java.sql.Timestamp] (
      d => new java.sql.Timestamp(d.getTime),
      d => new java.util.Date(d.getTime))

  class  UserPaymentTable(tag: Tag) extends Table[UserPayment](tag, "userPayment") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("userId")
    def paymentType = column[String]("paymentType")
    def provider = column[String]("provider")
    def accountNumber = column[String]("accountNumber")
    def expiry = column[Date]("expiry")
    def * = (id, userId, paymentType, provider, accountNumber, expiry) <> ((UserPayment.apply _).tupled, UserPayment.unapply)
  }

  val userPay = TableQuery[UserPaymentTable]

  def list(): Future[Seq[UserPayment]] = db.run {
    userPay.result
  }

  def delete(id: Long): Future[Unit] = db.run(userPay.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(userPay.filter(_.id === id).delete)

  def getById(id: Long): Future[UserPayment] = db.run {
    userPay.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: UserPayment): Future[Unit] = {
    val toUpdate: UserPayment = newElement.copy(id)
    db.run(userPay.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: UserPayment): Future[Int] = {
    val toUpdate: UserPayment = newElement.copy(id)
    db.run(userPay.filter(_.id === id).update(toUpdate))
  }

  def create(userId: Long, paymentType: String, provider: String, accountNumber: String, expiry: Date): Future[UserPayment] = db.run {
    (userPay.map(el => (el.userId, el.paymentType, el.provider, el.accountNumber, el.expiry))
      returning userPay.map(_.id)
      into {case ((userId, paymentType, provider, accountNumber, expiry),id) => UserPayment(id, userId, paymentType, provider, accountNumber, expiry)}
      ) += (userId, paymentType, provider, accountNumber, expiry)
  }
}
