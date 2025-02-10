package model

import domain.{AppNotUpdated, SuccessTransactionResult, TransactionResult}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile
import utils.StringUtils

import scala.concurrent.{ExecutionContext, Future}

case class OAuthApp(
  id: Option[Long],
  clientId: String,
  accessToken: Option[String],
  userId: Option[Long],
)

class OAuthAppTable(tag: Tag) extends Table[OAuthApp](tag, None, "OAUTH_APP") {
  val id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
  val clientId = column[String]("CLIENT_ID")
  val accessToken = column[Option[String]]("ACCESS_TOKEN")
  val userId = column[Option[Long]]("USER_ID")

  def * = (id, clientId, accessToken, userId).mapTo[OAuthApp]
}

class OAuthAppDao(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  val apps = TableQuery[OAuthAppTable]
  val users = TableQuery[UserTable]

  def listAll: Future[Seq[OAuthApp]] = db.run(apps.result)

  def add(oAuthApp: OAuthApp): Future[Int] = db.run(apps += oAuthApp)

  def findByClientId(clientId: String): Future[Option[OAuthApp]] =
    db.run(apps.filter(_.clientId === clientId).result.headOption)

  def findByToken(token: Option[String]): Future[Option[OAuthApp]] =
    db.run(apps.filter(_.accessToken === token).result.headOption)

  def update(clientId: String, userId: Option[Long]): Future[TransactionResult] = {
    val newToken = StringUtils.generateUuid()
    val query = apps
      .filter(_.clientId === clientId)
      .map(app => (app.accessToken, app.userId))
      .update((Option(newToken), userId))
    db.run(query).map { res =>
      println(res)
      if (res > 0) SuccessTransactionResult(newToken) else AppNotUpdated
    }
  }

}
