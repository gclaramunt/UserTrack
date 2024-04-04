val Http4sVersion      = "0.23.25"
val Specs2Version      = "4.20.5"
val catsEffectsVersion = "3.5.3"
val LogbackVersion     = "1.4.14"
val doobieVersion      = "1.0.0-RC5"

lazy val root = (project in file("."))
  .settings(
    organization := "gclaramunt",
    name         := "garbanzo-user-track",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"            %% "http4s-circe"        % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "org.tpolecat"          %% "doobie-core"         % doobieVersion,
      "com.h2database"         % "h2"                  % "2.2.224",
      "org.tpolecat"          %% "doobie-h2"           % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"     % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"       % doobieVersion,
      "com.github.pureconfig" %% "pureconfig"          % "0.17.4",
      "org.typelevel"         %% "cats-effect"         % catsEffectsVersion,
      "org.specs2"            %% "specs2-core"         % Specs2Version % Test,
      "org.tpolecat"          %% "doobie-specs2"       % doobieVersion % Test, // Specs2 support for typechecking statements.
      "ch.qos.logback"         % "logback-classic"     % LogbackVersion,
    ),
    // compiler plugins
    addCompilerPlugin(
      "org.typelevel"              %% "kind-projector"     % "0.13.2" cross CrossVersion.full
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  )
scalacOptions := Seq("-deprecation", "-feature")

Compile / run / fork := true

