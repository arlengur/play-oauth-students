package model

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class User(
  id: Option[Long],
  login: String,
  password: String,
)

class UserTable(tag: Tag) extends Table[User](tag, "USERS") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def login = column[String]("LOGIN")

  def password = column[String]("PASSWORD")

  override def * = (id.?, login, password).mapTo[User]
}

class UserDao(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  val users = TableQuery[UserTable]

  def listAll: Future[List[User]] = db.run(users.to[List].result)

  def add(user: User): Future[Long] = db.run(users returning users.map(_.id) += user)

  /** Filter user
    */
  private def filterQuery(login: String, pass: String): Query[UserTable, User, Seq] =
    users.filter(u => u.login === login && u.password === pass)

  /** Update user
    */
  def checkCredentials(login: String, password: String): Future[Option[User]] =
    db.run(filterQuery(login, password).result.headOption)

}
