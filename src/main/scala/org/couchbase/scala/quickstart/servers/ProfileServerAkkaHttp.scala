package org.couchbase.scala.quickstart.servers

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import org.couchbase.scala.quickstart.Endpoints
import org.couchbase.scala.quickstart.Endpoints.openapiYamlDocumentation
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.SwaggerUI

import scala.concurrent.Future

object ProfileServerAkkaHttp {

  implicit val system = ActorSystem(Behaviors.empty, "akka-http-actor-system")
  implicit val executionContext = system.executionContext

  var bindingFuture: Future[ServerBinding]  = Future.failed(new IllegalStateException("Server not yet started"))

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

  val helloRoute =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "Hello, World!"))
      }
    }

// TODO: probably remove, but use for sanity checking
  val swaggerRoute =
    AkkaHttpServerInterpreter().toRoute(Endpoints.swaggerFutureEndpoints)

  val swaggerOpenAPIRoute: Route = AkkaHttpServerInterpreter().toRoute(
    SwaggerUI[Future](openapiYamlDocumentation)
  )

  def startAkkaHttpServer(): Unit = {
    bindingFuture = Http().newServerAt("localhost", 8081).bind(getProfileRoute ~ postProfileRoute ~ swaggerOpenAPIRoute ~ helloRoute)
  }

  def stopAkkaHttpServer(): Unit = {
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}
