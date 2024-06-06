ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "3.3.3"

val circeVersion = "0.14.7"

lazy val root = (project in file("."))
  .settings(
    name := "scala_helper",
    idePackagePrefix := Some("io.daniel"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
      "org.tpolecat" %% "skunk-core" % "0.6.4",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe" % "config" % "1.4.3",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.6"
    )
  )
