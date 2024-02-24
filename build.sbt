val Http4sVersion = "0.23.25"
val Specs2Version = "4.20.5"
val catsEffectsVersion = "3.5.3"
val LogbackVersion = "1.2.11"
val doobieVersion = "1.0.0-RC5"

lazy val root = (project in file("."))
  .settings(
    organization := "gclaramunt",
    name := "narrative-user-track",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "com.h2database" % "h2" % "1.4.197",
      "org.tpolecat" %% "doobie-h2" % doobieVersion,
      "org.typelevel" %% "cats-effect" % catsEffectsVersion,
      "org.specs2" %% "specs2-core" % Specs2Version % "test",
      "org.tpolecat" %% "doobie-specs2" % doobieVersion % "test", // Specs2 support for typechecking statements.
      "ch.qos.logback" % "logback-classic" % LogbackVersion
    ),
    // compiler plugins
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
scalacOptions := Seq("-deprecation", "-feature")

Compile / run / fork := true

/*
  val startH2Task = TaskKey[Unit]("start-h2", "Starts H2 DB")
  val stopH2Task = TaskKey[Unit]("stop-h2", "Stops H2 DB")
  val h2tasks:Seq[Setting[_]] = Seq(startH2Task := {
    org.h2.tools.Server.createTcpServer().start();
    org.h2.tools.Server.createWebServer().start    // this starts the "web tool"
  }, stopH2Task :={
    org.h2.tools.Server.shutdownTcpServer("tcp://localhost:9092","",true,true);
} )
 */
