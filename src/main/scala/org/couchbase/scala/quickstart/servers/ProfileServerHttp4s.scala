package org.couchbase.scala.quickstart.servers

import cats.effect.unsafe.IORuntime
import cats.effect.{FiberIO, IO}
import cats.syntax.semigroupk._
import org.couchbase.scala.quickstart.Endpoints
import org.couchbase.scala.quickstart.controllers.ProfileController
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.ExecutionContext

class ProfileServerHttp4s(profileController: ProfileController[IO]) {

  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  val getProfileRoute: HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.getProfile.serverLogic(pid => profileController.getProfile(pid))
    )
  }

  val postProfileRoute: HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.addProfile.serverLogic(profileInput =>
        profileController.postProfile(profileInput)
      )
    )
  }

  val deleteProfileRoute: HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.deleteProfile.serverLogic(pid =>
        profileController.deleteProfile(pid)
      )
    )
  }

  val profileListingRoute: HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(
      Endpoints.profileListing.serverLogic { case (limit, skip, search) =>
        profileController.profileListing(limit, skip, search)
      }
    )
  }

  val swaggerRoute: HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(
      SwaggerUI[IO](Endpoints.openapiYamlDocumentation)
    )
  }

  val routes: HttpRoutes[IO] =
    getProfileRoute <+> postProfileRoute <+> deleteProfileRoute <+> profileListingRoute <+> swaggerRoute

  def buildServerDefinition(): BlazeServerBuilder[IO] = {
    BlazeServerBuilder[IO]
      .bindHttp(8082, "localhost")
      .withHttpApp(
        Router("/" -> routes).orNotFound
      )
  }

  def startServer() = {
    buildServerDefinition().resource
      .use(_ => IO.never)
      .start
      .unsafeRunSync()
  }

  def stopServer[A](fiber: FiberIO[A]) = {
    fiber.cancel.unsafeRunSync()
  }

}
