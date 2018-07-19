val doobieVersion = "0.5.3"
val http4sVersion = "0.18.13"

lazy val root = project
  .in(file("."))
  .settings(
    name := "transfer-api",
    version := "0.1.0",

    scalaVersion := "2.12.6",

    scalacOptions += "-Ypartial-unification",

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "1.1.0",
      "org.typelevel" %% "cats-effect" % "0.10.1",

      "org.http4s" %% "http4s-blaze-server" % http4sVersion,

      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-h2" % doobieVersion,
      "com.h2database" % "h2" % "1.4.197",

      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"
    )
  )

addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.7" cross CrossVersion.binary)