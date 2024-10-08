// specify the main class to run
//Compile / mainClass := Some("io.daniel.Main")
//Compile / mainClass := Some("io.daniel.apps.RefundApp")
Compile / mainClass := Some("io.daniel.apps.ClubTransfer")

val catsCoreVersion   = "2.12.0"
val catsEffectVersion = "3.5.4"
val circeVersion      = "0.14.9"
val skunkVersion      = "0.6.4"
val pureConfigVersion = "0.17.7"
val scalaTestVersion  = "3.2.18"
val awsSdkVersion     = "2.26.30"
val slf4jVersion      = "2.0.13"
val scalaCsvVersion   = "2.0.0"

inThisBuild(
  List(
    version           := "0.1.0",
    scalaVersion      := "3.5.0",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixOnCompile := true
  )
)

scalacOptions ++= Seq(
  "-Wunused:all"
)

lazy val root = (project in file("."))
  .settings(
    name             := "bondi",
    idePackagePrefix := Some("io.daniel"),
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"       % catsCoreVersion,
      "org.typelevel"         %% "cats-effect"     % catsEffectVersion,
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
      "software.amazon.awssdk" % "ses"             % awsSdkVersion,
      "software.amazon.awssdk" % "dynamodb"        % awsSdkVersion,
      "org.slf4j"              % "slf4j-api"       % slf4jVersion,
      "org.slf4j"              % "slf4j-nop"       % slf4jVersion,
      "com.github.tototoshi"  %% "scala-csv"       % scalaCsvVersion,
      "javax.mail"             % "javax.mail-api"  % "1.6.2",
      "com.sun.mail"           % "javax.mail"      % "1.6.2",
      "javax.activation"       % "activation"      % "1.1.1"
    )
  )
