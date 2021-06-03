package models

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CategoryTable(tag: Tag) extends Table[Category](tag, "category") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def * = (id, name, description) <> ((Category.apply _).tupled, Category.unapply)
  }

  val category = TableQuery[CategoryTable]

  def list(): Future[Seq[Category]] = db.run {
    category.result
  }

  def delete(id: Long): Future[Unit] = db.run(category.filter(_.id === id).delete).map(_ => ())
  def deleteJson(id: Long): Future[Int] = db.run(category.filter(_.id === id).delete)

  def getById(id: Long): Future[Category] = db.run {
    category.filter(_.id === id).result.head
  }

  def update(id: Long, newElement: Category): Future[Unit] = {
    val toUpdate: Category = newElement.copy(id)
    db.run(category.filter(_.id === id).update(toUpdate)).map(_ => ())
  }
  def updateJson(id: Long, newElement: Category): Future[Int] = {
    val toUpdate: Category = newElement.copy(id)
    db.run(category.filter(_.id === id).update(toUpdate))
  }

  def create(name: String, description: String): Future[Category] = db.run {
    (category.map(el => (el.name, el.description))
      returning category.map(_.id)
      into {case ((name, description),id) => Category(id, name, description)}
      ) += (name, description)
  }

}
