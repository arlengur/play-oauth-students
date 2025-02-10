package controllers

import model.{OAuthApp, OAuthAppDao, Student, StudentDao}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import play.api.mvc._
import slick.jdbc.JdbcProfile
import views.html

import java.util.concurrent.TimeoutException
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StudentController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  cc: MessagesControllerComponents,
)(
  implicit ec: ExecutionContext
) extends MessagesAbstractController(cc)
    with HasDatabaseConfigProvider[JdbcProfile] {
  private val students = new StudentDao(dbConfigProvider)
  private val apps = new OAuthAppDao(dbConfigProvider)

  /** This result directly redirect to the application home.
    */
  val Home: Result = Redirect(routes.StudentController.list())

  /** Handle default path requests, redirect to students list
    */
  def index =
    Action {
      Home
    }

  def oauthToken(request: RequestHeader): Option[String] =
    request.session.get("Authorization").orElse(request.headers.get("Authorization")).map { k =>
      if (k.startsWith("Bearer ")) {
        k.substring(7)
      } else {
        k
      }
    }

  def isAllowed(request: RequestHeader): Future[Option[OAuthApp]] = apps.findByToken(oauthToken(request))

  /** Display the paginated list of students.
    */
  def list(
    page: Int
  ): Action[AnyContent] =
    Action.async { implicit request =>
      isAllowed(request).flatMap {
        _.map { _ =>
          students
            .list(page, 5)
            .map { pageEmp =>
              Ok(html.list(pageEmp))
            }
            .recover {
              case ex: TimeoutException =>
                Logger("StudentsController").error("Problem found in student list process")
                InternalServerError(ex.getMessage)
            }
        }.getOrElse(Future.successful(Results.Unauthorized("Client is not authorized.")))
      }
    }

  /** Display the 'edit form' of an existing student.
    */
  def edit(
    id: Long
  ): Action[AnyContent] =
    Action.async { implicit request =>
      isAllowed(request).flatMap {
        _.map { _ =>
          students.findById(id).map(student => Ok(html.edit(id, studentForm.fill(student)))).recover {
            case ex: TimeoutException =>
              Logger("StudentsController").error("Problem found in student edit process")
              InternalServerError(ex.getMessage)
          }
        }.getOrElse(Future.successful(Results.Unauthorized("Client is not authorized.")))
      }
    }

  /** Handle the 'edit form' submission
    */
  def update(
    id: Long
  ): Action[AnyContent] =
    Action.async { implicit request =>
      isAllowed(request).flatMap {
        _.map { _ =>
          studentForm
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(html.edit(id, formWithErrors))),
              student => {
                val futureStudentUpdate = students.update(id, student.copy(id = Some(id)))
                futureStudentUpdate
                  .map { _ =>
                    Ok("")
                  }
                  .recover {
                    case ex: TimeoutException =>
                      Logger("StudentController").error("Problem found in student update process")
                      InternalServerError(ex.getMessage)
                  }
              },
            )
        }.getOrElse(Future.successful(Results.Unauthorized("Client is not authorized.")))
      }
    }

  /** Display the 'new student form'.
    */
  def create: Action[AnyContent] =
    Action.async { implicit request =>
      isAllowed(request).map {
        _.map { _ =>
          Ok(html.create(studentForm))
        }.getOrElse(Results.Unauthorized("Client is not authorized."))
      }
    }

  /** Handle the 'new student form' submission.
    */
  def save: Action[AnyContent] =
    Action.async { implicit request =>
      isAllowed(request).flatMap {
        _.map { _ =>
          studentForm
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(html.create(formWithErrors))),
              student => {
                val futureStudentInsert = students.insert(student)
                futureStudentInsert
                  .map(_ => Home.flashing("success" -> "Student %s has been added".format(student.firstName)))
                  .recover {
                    case ex: TimeoutException =>
                      Logger("StudentsController").error("Problem found in student save process")
                      InternalServerError(ex.getMessage)
                  }
              },
            )
        }.getOrElse(Future.successful(Results.Unauthorized("Client is not authorized.")))
      }
    }

  /** Handle student deletion
    */
  def delete(
    id: Long
  ): Action[AnyContent] =
    Action.async { implicit request =>
      isAllowed(request).flatMap {
        _.map { _ =>
          students.delete(id).map(_ => Home.flashing("success" -> "Student has been deleted")).recover {
            case ex: TimeoutException =>
              Logger("StudentController").error("Problem found in student delete process")
              InternalServerError(ex.getMessage)
          }
        }.getOrElse(Future.successful(Results.Unauthorized("Client is not authorized.")))
      }
    }

  def getStudentsJson =
    Action.async { implicit request =>
      isAllowed(request).flatMap {
        _.map { _ =>
          implicit val studentFormat = Json.format[Student]
          students.listAll map { students =>
            Ok(Json.toJson(students))
          }
        }.getOrElse(Future.successful(Results.Unauthorized("Client is not authorized.")))
      }

    }

  /** Describe the student form (used in both edit and create screens).
    */
  val studentForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "first-name" -> nonEmptyText,
      "middle-name" -> nonEmptyText,
      "last-name" -> nonEmptyText,
      "group" -> number,
      "avg-score" -> of(doubleFormat),
    )(Student.apply)(Student.unapply)
  )
}
