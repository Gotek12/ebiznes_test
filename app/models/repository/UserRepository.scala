package models.repository

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordInfo
import javax.inject.{Inject, Singleton}
import models.User2
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, implicit val classTag: ClassTag[PasswordInfo]) extends IdentityService[User2] {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  case class UserDto(id: Long, providerId: String, providerKey: String, email: String)

  class UserTable(tag: Tag) extends Table[UserDto](tag, "user2") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def providerId = column[String]("providerId")

    def providerKey = column[String]("providerKey")

    def email = column[String]("email")

    def * = (id, providerId, providerKey, email) <> ((UserDto.apply _).tupled, UserDto.unapply)
  }

  val user = TableQuery[UserTable]

  override def retrieve(loginInfo: LoginInfo): Future[Option[User2]] = db.run {
    user.filter(_.providerId === loginInfo.providerID)
      .filter(_.providerKey === loginInfo.providerKey)
      .result
      .headOption
  }.map(_.map(dto => toModel(dto)))

  def create(providerId: String, providerKey: String, email: String): Future[User2] = db.run {
    (user.map(c => (c.providerId, c.providerKey, c.email))
      returning user.map(_.id)
      into { case ((providerId, providerKey, email), id) => UserDto(id, providerId, providerKey, email) }
      ) += (providerId, providerKey, email)
  }.map(dto => toModel(dto))

  def getAll: Future[Seq[User2]] = db.run {
    user.result
  }.map(_.map(dto => toModel(dto)))

  def getByIdOption(id: Long): Future[Option[User2]] = db.run {
    user.filter(_.id === id).result.headOption
  }.map(_.map(dto => toModel(dto)))

  def getById(id: Long): Future[User2] = db.run {
    user.filter(_.id === id).result.head
  }.map(dto => toModel(dto))

  def update(id: Long, newUser: User2): Future[User2] = {
    val userToUpdate = newUser.copy(id)
    db.run {
      user.filter(_.id === id)
        .update(toDto(userToUpdate))
        .map(_ => userToUpdate)
    }
  }

  def delete(id: Long): Future[Unit] =
    db.run {
      user.filter(_.id === id)
        .delete
        .map(_ => ())
    }

  private def toModel(dto: UserDto): User2 =
    User2(dto.id, LoginInfo(dto.providerId, dto.providerKey), dto.email)

  private def toDto(model: User2): UserDto =
    UserDto(model.id, model.loginInfo.providerID, model.loginInfo.providerKey, model.email)
}