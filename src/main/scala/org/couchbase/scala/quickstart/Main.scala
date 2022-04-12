package org.couchbase.scala.quickstart

import akka.http.scaladsl.Http.ServerBinding
import cats.effect.{Fiber, FiberIO}
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.controllers.{CouchbaseProfileController, IOProfileController}
import org.couchbase.scala.quickstart.servers.{ProfileServerAkkaHttp, ProfileServerHttp4s, ProfileServerPlay}
import pureconfig.ConfigSource

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    // This code has deliberately not been refactored to use dependency injection, to make the flow more explicit.
    // Feel free to use your favourite dependency injection framework (I recommend MacWire) to refactor this code.

    // Read configuration needed for Couchbase and the three web servers (Akka HTTP, http4s, Play).
    lazy val quickstartConfig: QuickstartConfig =
      ConfigSource.default.load[QuickstartConfig] match {
        case Left(err) =>
          throw new Exception(
            s"Unable to load system configuration. Please check application.conf. Error: $err"
          )
        case Right(config) => config
      }

    // Set up connection to Couchbase. Note that this assumes Couchbase is already running locally!
    lazy val couchbaseConnection = new ProdCouchbaseConnection(quickstartConfig)

    lazy val profileController =
      new CouchbaseProfileController(couchbaseConnection, quickstartConfig)

    lazy val akkaServer = new ProfileServerAkkaHttp(profileController)
    lazy val http4sServer = new ProfileServerHttp4s(
      new IOProfileController(profileController)
    )
    lazy val playProfileServer = new ProfileServerPlay(profileController)


    // Set up the indexes and collections. This can take a bit.
    Await.result(couchbaseConnection.bucket, 30.seconds)
    Await.result(profileController.setupIndexesAndCollections(), 30.seconds)

    // Start servers!
    // Note that we only start a server when there is accompanying config in application.conf.

    // Start Akka HTTP server:
    val akkaFutureBinding: Option[Future[ServerBinding]] =
      quickstartConfig.akkaHttp match {
        case Some(akkaConfig) =>
          val binding = akkaServer.startAkkaHttpServer()
          println(
            s"Akka running, see http://localhost:${akkaConfig.port}/docs for the Swagger UI"
          )
          Some(binding)

        case None =>
          println("Akka HTTP config not found. Server will not be started")
          None
      }

    // Start Http4s server:
    val http4sFiber: Option[FiberIO[Nothing]] = quickstartConfig.http4s match {
      case Some(http4sConfig) =>
        val fiber = http4sServer.startServer(http4sServer.buildServerDefinition())
        println(s"http4s server running, see http://localhost:${http4sConfig.port}/docs")
        Some(fiber)
      case None =>
        println("http4s config not found. Server will not be started")
        None
    }

    // Start Play server:
    val playHttpServer = quickstartConfig.play match {
      case Some(playConfig) =>
        val server = playProfileServer.startServer()
        println(s"Play server running, see http://localhost:${playConfig.port}/docs")
        Some(server)
      case None =>
        println("Play config not found. Server will not be started")
        None
    }

    // Wait for input to stop the servers from immediately being wound down.
    StdIn.readLine()

    // Stop all servers:
    akkaFutureBinding foreach akkaServer.stopAkkaHttpServer
    http4sFiber foreach http4sServer.stopServer
    playHttpServer foreach playProfileServer.stopServer
  }
}
