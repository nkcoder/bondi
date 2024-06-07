ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "3.3.3"

// specify the main class to run
//Compile / mainClass := Some("io.daniel.Main")
//Compile / mainClass := Some("io.daniel.apps.RefundApp")

val circeVersion      = "0.14.7"
val skunkVersion      = "0.6.4"
val pureConfigVersion = "0.17.6"
val scalaTestVersion  = "3.2.18"
val awsSdkVersion     = "2.25.68"
val slf4jVersion      = "2.0.13"

lazy val root = (project in file("."))
  .settings(
    name             := "bondi",
    idePackagePrefix := Some("io.daniel"),
    libraryDependencies ++= Seq(
      "org.scalatest"         %% "scalatest"       % scalaTestVersion % Test,
      "org.tpolecat"          %% "skunk-core"      % skunkVersion,
      "io.circe"              %% "circe-core"      % circeVersion,
      "io.circe"              %% "circe-generic"   % circeVersion,
      "io.circe"              %% "circe-parser"    % circeVersion,
      "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion,
      "software.amazon.awssdk" % "s3"              % awsSdkVersion,
      "software.amazon.awssdk" % "secretsmanager"  % awsSdkVersion,
      "software.amazon.awssdk" % "sqs"             % awsSdkVersion,
      "software.amazon.awssdk" % "sns"             % awsSdkVersion,
      "org.slf4j"              % "slf4j-api"       % slf4jVersion,
      "org.slf4j"              % "slf4j-nop"       % slf4jVersion
    )
  )
