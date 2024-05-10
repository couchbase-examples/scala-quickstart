package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.controllers.CouchbaseAirlineController
import pureconfig.ConfigSource
import sttp.tapir.server.netty.{NettyFutureServer, NettyFutureServerInterpreter}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}
import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
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

    lazy val airlineController =
      new CouchbaseAirlineController(couchbaseConnection, quickstartConfig)


    // Set up the indexes and collections. This can take a bit.
    Await.result(couchbaseConnection.bucket, 30.seconds)

    val swaggerRoute = NettyFutureServerInterpreter().toRoute(Endpoints.swaggerEndpoints)(ExecutionContext.global)

    // Start the server!
    val server = Await.result(NettyFutureServer()(ExecutionContext.global)
      .port(quickstartConfig.netty.port)
      .addRoute(swaggerRoute)
      .addEndpoint(Endpoints.airlineListing.serverLogic(airlineController.airlineListing _))
      .addEndpoint(Endpoints.getAirline.serverLogic(airlineController.getAirline _))
      .addEndpoint(Endpoints.postAirline.serverLogic(airlineController.postAirline _))
      .addEndpoint(Endpoints.putAirline.serverLogic(airlineController.putAirline _))
      .addEndpoint(Endpoints.deleteAirline.serverLogic(airlineController.deleteAirline _))
      .start(), 1.seconds)

    // Wait for input to stop the servers from immediately being wound down.
    StdIn.readLine()

    // Stop the server:
    server.stop()
  }
}
