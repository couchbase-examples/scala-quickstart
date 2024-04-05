package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}
import sttp.tapir._
import sttp.tapir.docs.openapi._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.openapi.circe.yaml._

import java.util.UUID
import java.util.concurrent.Future

object Endpoints {
  // Base end point for Profiles.
  // /api/v1/profile
  val profileBaseEndpoint = endpont
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
    .out(jsonBody[List[Profile]])
    .errorOut(stringBody)

  def endpoints = List(postProfile, getProfile, deleteProfile, profileListing)
  def swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[Future](
      endpoints,
      "Couchbae profile API",
      "1.0"
    )

}
