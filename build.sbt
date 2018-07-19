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

      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )

addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.7" cross CrossVersion.binary)