package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.controllers.CouchbaseProfileController
import pureconfig.ConfigSource
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.StdIn
import sttp.tapir.server.netty.{FutureRoute, NettyFutureServer, NettyFutureServerInterpreter}

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

    lazy val profileController =
      new CouchbaseProfileController(couchbaseConnection, quickstartConfig)


    // Set up the indexes and collections. This can take a bit.
    Await.result(couchbaseConnection.bucket, 30.seconds)
    Await.result(profileController.setupIndexesAndCollections(), 30.seconds)

    val swaggerRoute = NettyFutureServerInterpreter().toRoute(Endpoints.swaggerEndpoints)(ExecutionContext.global)

    // Start the server!
    val server = Await.result(NettyFutureServer()(ExecutionContext.global)
      .port(quickstartConfig.netty.port)
      .addRoute(swaggerRoute)
      .addEndpoint(Endpoints.profileListing.serverLogic(profileController.profileListing _))
      .addEndpoint(Endpoints.getProfile.serverLogic(profileController.getProfile _))
      .addEndpoint(Endpoints.postProfile.serverLogic(profileController.postProfile _))
      .addEndpoint(Endpoints.putProfile.serverLogic(profileController.putProfile _))
      .addEndpoint(Endpoints.deleteProfile.serverLogic(profileController.deleteProfile _))
      .start(), 1.seconds)

    // Wait for input to stop the servers from immediately being wound down.
    StdIn.readLine()

    // Stop the server:
    server.stop()
  }
}
