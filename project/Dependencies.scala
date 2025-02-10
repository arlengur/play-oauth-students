import sbt.*

object Dependencies {

  val scalaLoggingVersion    = "3.9.4"
  val logbackVersion         = "1.5.6"
  val logstashVersion        = "7.4"
  val slickVersion           = "3.5.1"
  val circeVersion           = "0.14.1"
  val pgVersion              = "42.3.3"
  val slickPg                = "0.22.2"

  val core = {

    lazy val slick       = "com.typesafe.slick" %% "slick"          % slickVersion
    lazy val tsSlick     = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion

    lazy val postgreSql = List(
      "org.postgresql"       % "postgresql"          % pgVersion,
      "com.github.tminglei" %% "slick-pg"            % slickPg,
      "com.github.tminglei" %% "slick-pg_circe-json" % slickPg,
    )
    lazy val circe = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
    ).map(_ % circeVersion)

    List(slick, tsSlick) ++ circe ++ postgreSql
  }

}
