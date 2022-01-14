ThisBuild / organization := "org.couchbase"
ThisBuild / scalaVersion := "2.13.7"
ThisBuild / version := "0.1.0"

//wartremoverErrors ++= Warts.unsafe
//wartremoverErrors ++= Warts.all

lazy val root = project
  .in(file("."))
  .settings(
    name := "Couchbase Scala Quickstart"
  )

val akkaVersion = "2.6.18"
val akkaHttpVersion = "10.2.7"
val circeVersion = "0.14.1"
val macwireVersion = "2.5.3"
// Stuck on 0.18.3 until there's a new release for tapir-swagger-ui-http4s.
val tapirVersion = "0.19.3"

libraryDependencies ++= Seq(
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
  "com.softwaremill.macwire" %% "util" % macwireVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
//  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % tapirVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,

)

// If you're using Scala 2.12: then the following flag is useful for Tapir, to prevent having to annotate type arguments.
//scalacOptions += "-Ypartial-unification"

