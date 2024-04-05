package org.couchbase.scala.quickstart.servers

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.couchbase.scala.quickstart.Endpoints
import org.couchbase.scala.quickstart.controllers.ProfileController
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future

class ProfileServerAkkaHttp(profileController: ProfileController[Future]) {

  implicit val system = ActorSystem(Behaviors.empty, "akka-http-actor-system")
  implicit val executionContext = system.executionContext

  val getProfileRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.getProfile.serverLogic(pid => profileController.getProfile(pid))
    )

  val postProfileRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.postProfile.serverLogic(profile =>
        profileController.postProfile(profile)
      )
    )

  val putProfileRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.putProfile.serverLogic { case (pid, profileInput) =>
        profileController.putProfile(pid, profileInput)
      }
    )

  val deleteProfileRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      Endpoints.deleteProfile.serverLogic(pid =>
        profileController.deleteProfile(pid)
      )
    )

  val profileListingRoute: Route =
    AkkaHttpServerInterpreter().toRoute(Endpoints.profileListing.serverLogic {
      case (limit, skip, search) =>
        profileController.profileListing(limit, skip, search)
    })

  val swaggerOpenAPIRoute: Route = AkkaHttpFutureServerInterpreter().toRoute(Endpoints.swaggerEndpoints)

  def startAkkaHttpServer(): Future[ServerBinding] = {
    Http()
      .newServerAt("localhost", 8081)
      .bind(
        getProfileRoute ~ postProfileRoute ~ putProfileRoute ~ profileListingRoute ~ swaggerOpenAPIRoute
      )
  }

  def stopAkkaHttpServer(bindingFuture: Future[ServerBinding]): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
