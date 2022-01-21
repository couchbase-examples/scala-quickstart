ThisBuild / organization := "org.couchbase"
ThisBuild / scalaVersion := "2.13.8"
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
val http4sVersion = "0.23.7"
val macwireVersion = "2.5.3"
val playVersion = "2.8.13"
val tapirVersion = "0.19.3"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "com.couchbase.client" %% "scala-client" % "1.2.4",
  "com.github.pureconfig" %% "pureconfig" % "0.17.1",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.3.0",
  "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided",
  "com.softwaremill.macwire" %% "util" % macwireVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-play-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "org.typelevel" %% "cats-core" % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.4",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.play" %% "play-netty-server" % playVersion,
  "com.typesafe.play" %% "play-server" % playVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
)

// If you're using Scala 2.12: then the following flag is useful for Tapir, to prevent having to annotate type arguments.
//scalacOptions += "-Ypartial-unification"

