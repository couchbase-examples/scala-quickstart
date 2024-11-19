ThisBuild / scalacOptions ++= Seq("-deprecation")
ThisBuild / organization := "org.couchbase"
ThisBuild / scalaVersion := "2.13.9"
ThisBuild / version := "0.2.0"

// GitHub Action related settings
// To stop generating the GitHub publish action:
//ThisBuild / githubWorkflowPublishTargetBranches := Seq()


//wartremoverErrors ++= Warts.unsafe
//wartremoverErrors ++= Warts.all


lazy val root = project
  .in(file("."))
  .settings(
    name := "Couchbase Scala Quickstart"
  )

val circeVersion = "0.14.6"
val tapirVersion = "1.10.5"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.5.3",
  "com.couchbase.client" %% "scala-client" % "1.6.0",
  "com.github.pureconfig" %% "pureconfig" % "0.17.6",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-netty-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % "0.14.3" excludeAll(
    ExclusionRule(organization="io.circe", name="circe-core"),
    ExclusionRule(organization="io.circe", name="circe-generic")
  ),
//  "io.circe" %% "circe-derivation" % "0.13.0-M5" excludeAll(
//    ExclusionRule(organization="io.circe", name="circe-core"),
//    ExclusionRule(organization="io.circe", name="circe-generic")
//  ),
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalatest" %% "scalatest" % "3.2.18" % "test",
)

// If you're using Scala 2.12: then the following flag is useful for Tapir, to prevent having to annotate type arguments.
//ThisBuild / scalacOptions += "-Ypartial-unification"

