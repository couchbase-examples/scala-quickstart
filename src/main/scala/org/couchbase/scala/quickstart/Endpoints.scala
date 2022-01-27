package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.openapi.circe.yaml._

import java.util.UUID

object Endpoints {
  // HealthCheck:
  // GET /api/v1/health
  val healthCheck = endpoint.get
    .in("api" / "v1" / "health")

  // Base end point for Profiles.
  // /api/v1/profile
  val profileBaseEndpoint = endpoint
    .in("api" / "v1" / "profile")

  // POST /profile
  val addProfile = profileBaseEndpoint.post
    .in(jsonBody[ProfileInput])
    .errorOut(stringBody)
    .out(jsonBody[Profile])

  // GET /profile?id=uuid
  // /{id}
  val getProfile = profileBaseEndpoint.get
    .in(query[UUID]("id"))
    .out(jsonBody[Profile])
    .errorOut(stringBody)

  // DELETE /profile?id=uuid
  val deleteProfile = profileBaseEndpoint.delete
    .in(query[UUID]("id"))
    .out(jsonBody[UUID])
    .errorOut(stringBody)

  // GET /profiles?limit=m&skip=n&search=string
  val profileListing = profileBaseEndpoint.get
    .in("profiles")
    .in(query[Option[Int]]("limit"))
    .in(query[Option[Int]]("skip"))
    .in(query[String]("search"))
    .out(jsonBody[List[Profile]])
    .errorOut(stringBody)

  def openapiYamlDocumentation: String = OpenAPIDocsInterpreter()
    .toOpenAPI(
      List(addProfile, getProfile, deleteProfile, profileListing),
      "Couchbase profile API",
      "1.0"
    )
    .toYaml

}
