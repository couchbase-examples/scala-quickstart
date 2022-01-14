package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir._

import java.util.UUID
import scala.concurrent.Future

object Endpoints {

  // TODO: hardcode capabilities?

  // HealthCheck: /api/v1/health

  // GET
  val healthCheck = endpoint.get
    .in("health")

  //  @RequestMapping("/api/v1/profile"

  val profileBaseEndpoint = endpoint
    .in("api" / "v1" / "profile")

  // POST
  val addProfile = profileBaseEndpoint.post
    .in(jsonBody[ProfileInput])
    .errorOut(stringBody)
    .out(jsonBody[Profile])
//    .out([UUID])
//    .in(header[String](name = "X-Auth-Token"))
// fix status code

  // GET/DELETE
  // /{id}
  val getProfile = profileBaseEndpoint.get
    .in(query[UUID]("id"))
    .out(jsonBody[Profile])
    .errorOut(stringBody)

  val deleteProfile = profileBaseEndpoint.delete
    .in(query[UUID]("id"))
    .out(jsonBody[Profile])
    .errorOut(stringBody)

  // GET
  // /profiles/
  val profileListing = profileBaseEndpoint.get
    .in("profiles")
    .out(jsonBody[List[Profile]])

  // TODO: probably not needed
  val swaggerFutureEndpoints = SwaggerInterpreter().fromEndpoints[Future](
    List(addProfile, getProfile, deleteProfile, profileListing),
    "Couchbase profile API",
    "1.0"
  )


  def openapiYamlDocumentation: String = {
    import sttp.tapir.docs.openapi._
    import sttp.tapir.openapi.circe.yaml._

    // TODO: verify whether the default options are okay
    val docs: OpenAPI = OpenAPIDocsInterpreter().toOpenAPI(
      List(addProfile, getProfile, deleteProfile, profileListing),
      "Couchbase profile API",
      "1.0"
    )
    docs.toYaml
  }
}
