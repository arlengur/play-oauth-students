import sbt.Keys.libraryDependencies

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "play-oauth-students",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.16",
    libraryDependencies ++= Seq(
      guice,
      "com.h2database" % "h2" % "2.3.232",
      "org.playframework" %% "play-slick" % "6.1.1",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.3.1",
    ),
  )
