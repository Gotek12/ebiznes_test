package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import java.sql.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class  UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def nick = column[String]("nick")
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def email = column[String]("email")
    def password = column[String]("password")
    def registeredAt = column[Option[Date]]("registeredAt")
    def lastLogin = column[Option[Date]]("lastLogin")
    def * = (id, nick, firstName, lastName, email, password, registeredAt, lastLogin) <> ((User.apply _).tupled, User.unapply)
  }

  val user = TableQuery[UserTable]

  def list(): Future[Seq[User]] = db.run {
    user.result
  }

  def delete(id: Long): Future[Unit] = db.run(user.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(user.filter(_.id === id).delete)

  def getById(id: Long): Future[User] = db.run {
    user.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: User): Future[Unit] = {
    val toUpdate: User = newElement.copy(id)
    db.run(user.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: User): Future[Int] = {
    val toUpdate: User = newElement.copy(id)
    db.run(user.filter(_.id === id).update(toUpdate))
  }

  def create(nick: String, firstName: String, lastName: String, email: String, password: String, registeredAt: Option[Date], lastLogin: Option[Date]): Future[User] = db.run {
    (user.map(el => (el.nick, el.firstName, el.lastName, el.email, el.password, el.registeredAt, el.lastLogin))
      returning user.map(_.id)
      into {case ((nick, firstName, lastName, email, password, registeredAt, lastLogin),id) => User(id, nick, firstName, lastName, email, password, registeredAt, lastLogin)}
      ) += (nick, firstName, lastName, email, password, registeredAt, lastLogin)
  }
}
