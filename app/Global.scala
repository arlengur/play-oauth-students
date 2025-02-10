import com.google.inject.AbstractModule
import model._
import play.api.db.slick.DatabaseConfigProvider

import javax.inject._
import scala.concurrent.ExecutionContext

class Module extends AbstractModule {
  override def configure(): Unit = bind(classOf[OnStartup]).asEagerSingleton()
}

@Singleton
class OnStartup @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val students = new StudentDao(dbConfigProvider)
  students.listAll map { list =>
    if (list.isEmpty) {
      val defaultStudents = Seq(
        Student(Option(1L), "Михаил", "Анатольевич", "Гай", 304, 5.0),
        Student(Option(1L), "Николай", "Сергеевич", "Борзов", 304, 4.5),
      )
      defaultStudents.map(students.add)
    }
  }
  val oAuthApps = new OAuthAppDao(dbConfigProvider)
  oAuthApps.listAll map { list =>
    if (list.isEmpty) {
      val defaultApps = Seq(
        OAuthApp(Option(1L), "clientId", None, None)
      )
      defaultApps.map(oAuthApps.add)
    }
  }
  val users = new UserDao(dbConfigProvider)
  users.listAll map { list =>
    if (list.isEmpty) {
      val defaultUsers = Seq(
        User(Option(1L), "user", "pass")
      )
      defaultUsers.map(users.add)
    }
  }
}
