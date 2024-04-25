package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput, ProfileListingInput, PutProfileInput}
import sttp.model.QueryParams
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsOptions
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.util.UUID
import scala.concurrent.Future

object Endpoints {
  // Base end point for Profiles.
  // /api/v1/profile
  val profileBaseEndpoint = endpoint
    .in("api" / "v1" / "profile")

  // GET /profile?id=uuid
  val getProfile = profileBaseEndpoint.get
    .in(query[UUID]("id"))
    .out(jsonBody[Profile])
    .errorOut(stringBody)

  // POST /profile with JSON Profile body
  val postProfile = profileBaseEndpoint.post
    .in(jsonBody[ProfileInput])
    .errorOut(stringBody)
    .out(jsonBody[Profile])

  // PUT /profile?id=uuid with JSON Profile body
  val putProfile = profileBaseEndpoint.put
    .in(query[UUID]("id"))
    .in(jsonBody[ProfileInput])
    .mapIn(e => PutProfileInput(e._1, e._2))(ppi => (ppi.pid, ppi.profileInput))
    .errorOut(stringBody)
    .out(jsonBody[Profile])

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
    .mapIn(data => ProfileListingInput(data._1, data._2, data._3))(pli => (pli.limit, pli.skip, pli.search))
    .out(jsonBody[List[Profile]])
    .errorOut(stringBody)

  def endpoints = List(
    postProfile,
    getProfile,
    deleteProfile,
    profileListing
  )


  def swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[Future](
      endpoints,
      "Couchbase profile API",
      "1.0"
    )
}
