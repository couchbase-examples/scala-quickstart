package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.controllers._
import pureconfig.ConfigSource
import sttp.tapir.server.netty.{NettyFutureServer, NettyFutureServerInterpreter}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    // Read configuration needed for Couchbase and the three web servers (Akka HTTP, http4s, Play).
    implicit lazy val quickstartConfig: QuickstartConfig =
      ConfigSource.default.load[QuickstartConfig] match {
        case Left(err) =>
          throw new Exception(
            s"Unable to load system configuration. Please check application.conf. Error: $err"
          )
        case Right(config) => config
      }

    // Set up connection to Couchbase. Note that this assumes Couchbase is already running locally!
    implicit lazy val couchbaseConnection = new ProdCouchbaseConnection(quickstartConfig)

    var serverConfig = NettyFutureServer()(ExecutionContext.global)
      .port(quickstartConfig.netty.port)

    val controllers = List[ModelController[?]](
      new CouchbaseAirlineController(),
      new CouchbaseAirportController(),
      new CouchbaseHotelController(),
      new CouchbaseLandmarkController(),
      new CouchbaseRouteController()
    )

    val endpoints = controllers.map(_.endpoints()).flatMap(e => {
        serverConfig = serverConfig.addEndpoints(e._2)
        e._1
      })


    // Set up the indexes and collections. This can take a bit.
    Await.result(couchbaseConnection.bucket, 30.seconds)
    println("Connected to Couchbase cluster")

    val swaggerEndpoints = SwaggerInterpreter()
      .fromEndpoints[Future](
        endpoints,
        "Couchbase airline API",
        "1.0"
      )

    serverConfig = serverConfig.addRoute(NettyFutureServerInterpreter().toRoute(swaggerEndpoints)(ExecutionContext.global))
    // start the server
    val server = Await.result(serverConfig.start(), 1.seconds)

    // Wait for input to stop the servers from immediately being wound down.
    StdIn.readLine()

    // Stop the server:
    server.stop()
  }
}
