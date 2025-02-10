package model

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Page[A](
  items: Seq[A],
  page: Int,
  offset: Long,
  total: Long,
) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Student(
  id: Option[Long],
  firstName: String,
  middleName: String,
  lastName: String,
  groupId: Int,
  avgScore: Double,
)

class StudentTable(tag: Tag) extends Table[Student](tag, "STUDENTS") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

  def firstName = column[String]("FIRST_NAME")

  def middleName = column[String]("MIDDLE_NAME")

  def lastName = column[String]("LAST_NAME")

  def groupId = column[Int]("GROUP_ID")

  def avgScore = column[Double]("AVG_SCORE")

  override def * = (id.?, firstName, middleName, lastName, groupId, avgScore).mapTo[Student]
}

class StudentDao(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile] {

  val students = TableQuery[StudentTable]

  def add(student: Student): Future[Long] = db.run(students returning students.map(_.id) += student)

  def listAll: Future[List[Student]] = db.run(students.to[List].result)

  def list(page: Int, pageSize: Int): Future[Page[Student]] = {
    val offset = pageSize * page
    val query = students
      .drop(offset)
      .take(pageSize)
    val totalRows = count()
    val result = db.run(query.result)
    result flatMap (students => totalRows map (rows => Page(students, page, offset, rows)))
  }

  /** Count students with a filter
    */
  private def count(): Future[Int] = db.run(students.length.result)

  /** Filter student with id
    */
  private def filterQuery(id: Long): Query[StudentTable, Student, Seq] = students.filter(_.id === id)

  /** Find student by id
    */
  def findById(id: Long): Future[Student] = db.run(filterQuery(id).result.head)

  /** Create a new student
    */
  def insert(student: Student): Future[Int] = db.run(students += student)

  /** Update student with id
    */
  def update(id: Long, student: Student): Future[Int] = db.run(filterQuery(id).update(student))

  /** Delete student with id
    */
  def delete(id: Long): Future[Int] = db.run(filterQuery(id).delete)
}
