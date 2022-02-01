package org.couchbase.scala.quickstart.servers

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.couchbase.scala.quickstart.Endpoints
import org.couchbase.scala.quickstart.controllers.ProfileController
import play.api.routing.Router.Routes
import play.core.server.{NettyServer, Server, ServerConfig}
import sttp.tapir.server.play.PlayServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future

class ProfileServerPlay(profileController: ProfileController[Future]) {
  val actorSystem: ActorSystem = ActorSystem("play-server")
  implicit val materializer: Materializer =
    Materializer.matFromSystem(actorSystem)

  val getProfileRoute: Routes = PlayServerInterpreter().toRoutes(
    Endpoints.getProfile.serverLogic(pid => profileController.getProfile(pid))
  )

  val postProfileRoute: Routes = {
    PlayServerInterpreter().toRoutes(
      Endpoints.postProfile.serverLogic(profileInput =>
        profileController.postProfile(profileInput)
      )
    )
  }

  val putProfileRoute: Routes = {
    PlayServerInterpreter().toRoutes(
      Endpoints.putProfile.serverLogic { case (pid, profileInput) =>
        profileController.putProfile(pid, profileInput)
      }
    )
  }

  val deleteProfileRoute: Routes = {
    PlayServerInterpreter().toRoutes(
      Endpoints.deleteProfile.serverLogic(pid =>
        profileController.deleteProfile(pid)
      )
    )
  }

  val profileListingRoute: Routes = {
    PlayServerInterpreter().toRoutes(
      Endpoints.profileListing.serverLogic { case (limit, skip, search) =>
        profileController.profileListing(limit, skip, search)
      }
    )
  }

  val swaggerRoute: Routes = {
    PlayServerInterpreter().toRoutes(
      SwaggerUI[Future](Endpoints.openapiYamlDocumentation)
    )
  }

  val routes: Routes =
    getProfileRoute orElse postProfileRoute orElse putProfileRoute orElse deleteProfileRoute orElse profileListingRoute orElse swaggerRoute

  def startServer(): Server = {
    val playConfig = ServerConfig(port =
      sys.props.get("play.server.http.port").map(_.toInt).orElse(Some(8083))
    )
    NettyServer.fromRouterWithComponents(playConfig)(_ => routes)
  }

  def stopServer(server: Server): Unit = {
    server.stop()
  }
}
