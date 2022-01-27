package org.couchbase.scala.quickstart

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
    // Feel free to use your favourite dependency injection framework to clean up this code.

    lazy val quickstartConfig: QuickstartConfig = ConfigSource.default.load[QuickstartConfig] match {
      case Left(err) => throw new Exception(s"Unable to load system configuration. Please check application.conf. Error: $err")
      case Right(config) => config
    }

    lazy val couchbaseConnection = new ProdCouchbaseConnection(quickstartConfig)

    lazy val profileController = new CouchbaseProfileController(couchbaseConnection, quickstartConfig)
    Await.result(profileController.setupIndexes(), 10.seconds)

    println("Hello, World!")
    val akkaServer = new ProfileServerAkkaHttp(profileController)
    val akkaFuturebinding = akkaServer.startAkkaHttpServer()
    println("Akka running, see http://localhost:8081/docs for the Swagger UI")
    val http4sServer = new ProfileServerHttp4s(new IOProfileController(profileController))
    val http4sFiber = http4sServer.startServer()
    println("Http4s server running, see http://localhost:8082/docs")
    val playProfileServer = new ProfileServerPlay(profileController)
    val playHttpServer = playProfileServer.startServer()
    println("Play server running, see http://localhost:8083/docs")

    StdIn.readLine()
    akkaServer.stopAkkaHttpServer(akkaFuturebinding)
    http4sServer.stopServer(http4sFiber)
    playProfileServer.stopServer(playHttpServer)
  }
}
