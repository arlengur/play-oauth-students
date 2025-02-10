package controllers

import domain.{AppNotUpdated, SuccessTransactionResult}
import model.{Grant, OAuthAppDao, UserDao}
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc._
import views.html

import javax.inject.Inject
import scala.concurrent._

class OAuth2Controller @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  cc: MessagesControllerComponents,
)(
  implicit ec: ExecutionContext
) extends MessagesAbstractController(cc) {
  private val users = new UserDao(dbConfigProvider)
  private val apps = new OAuthAppDao(dbConfigProvider)

  def displayAuthorize(): Action[AnyContent] =
    Action.async { implicit request =>
      params("client_id") match {
        case Some(clientId) =>
          apps.findByClientId(clientId).map {
            case Some(_) =>
              Ok(views.html.authorize(grantForm.fill(Grant("", "", clientId))))
            case None => NotFound("No application registered for this key")
          }
        case None => Future.successful(BadRequest("Missing parameters client_id"))
      }
    }

  def authorize(): Action[AnyContent] =
    Action.async { implicit request =>
      grantForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(html.authorize(formWithErrors))),
          grant => {
            users.checkCredentials(grant.login, grant.password).flatMap {
              case Some(user) =>
                apps.update(grant.clientId, user.id).map {
                  case SuccessTransactionResult(accessToken) =>
                    Ok(views.html.unit(accessToken)).withSession("Authorization" -> s"Bearer $accessToken")
                  case AppNotUpdated => BadRequest("App not updated")
                }
              case None => Future.successful(BadRequest("User not found"))
            }
          },
        )
    }

  def denied() =
    Action {
      Ok("Access denied")
    }

  private[this] def params(name: String)(implicit request: Request[AnyContent]): Option[String] =
    request
      .getQueryString(name)
      .orElse(
        request.body.asFormUrlEncoded.flatMap(_.get(name).flatMap(_.headOption))
      )

  val grantForm = Form(
    mapping(
      "login" -> nonEmptyText,
      "password" -> nonEmptyText,
      "client-id" -> nonEmptyText,
    )(Grant.apply)(Grant.unapply)
  )

}
