package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.util.UUID
import scala.concurrent.Future

object Endpoints {
  // HealthCheck:
  // GET /api/v1/health
  val healthCheck = endpoint.get
    .in("api" / "v1" / "health")

  val profileBaseEndpoint: Endpoint[Unit, Unit, Unit, Unit, Any] = endpoint
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
    .out(jsonBody[UUID])
    .errorOut(stringBody)

  // GET
  // /profiles/
  val profileListing = profileBaseEndpoint.get
    .in("profiles")
    .in(query[Option[Int]]("limit"))
    .in(query[Option[Int]]("skip"))
    .in(query[String]("search"))
    .out(jsonBody[List[Profile]])
    .errorOut(stringBody)

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
