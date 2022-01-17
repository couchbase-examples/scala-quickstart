package org.couchbase.scala.quickstart.servers

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import org.couchbase.scala.quickstart.Endpoints
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future

object ProfileServerAkkaHttp {

  implicit val system = ActorSystem(Behaviors.empty, "akka-http-actor-system")
  implicit val executionContext = system.executionContext

  val getProfileRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.getProfile.serverLogic(pid =>
        Future.successful(ProfileController.getProfile(pid))
      )
    )

  val postProfileRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.addProfile.serverLogic(profile =>
        Future.successful(ProfileController.postProfile(profile))
      )
    )

  val deleteProfileRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.deleteProfile.serverLogic(pid =>
        Future.successful(ProfileController.deleteProfile(pid))
      )
    )

  val profileListingRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.profileListing.serverLogic(_ =>
        Future.successful(ProfileController.profileListing())
      )
    )

// TODO: probably remove, but use for sanity checking
  val swaggerRoute =
    AkkaHttpServerInterpreter().toRoute(Endpoints.swaggerFutureEndpoints)

  val swaggerOpenAPIRoute: Route = AkkaHttpServerInterpreter().toRoute(
    SwaggerUI[Future](Endpoints.openapiYamlDocumentation)
  )

  def startAkkaHttpServer(): Future[ServerBinding] = {
    Http()
      .newServerAt("localhost", 8081)
      .bind(
        getProfileRoute ~ postProfileRoute ~ profileListingRoute ~ swaggerOpenAPIRoute
      )
  }

  def stopAkkaHttpServer(bindingFuture: Future[ServerBinding]): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
