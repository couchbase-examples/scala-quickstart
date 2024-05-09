package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.models.{Airline, AirlineInput, AirlineListingInput, PutAirlineInput}
import sttp.model.QueryParams
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsOptions
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.util.UUID
import scala.concurrent.Future

object Endpoints {
  // Base end point for Airlines.
  // /api/v1/airline
  val airlineBaseEndpoint = endpoint
    .in("api" / "v1" / "airline")

  // GET /airline?id=uuid
  val getAirline = airlineBaseEndpoint.get
    .in(query[UUID]("id"))
    .out(jsonBody[Airline])
    .errorOut(stringBody)

  // POST /airline with JSON Profile body
  val postAirline = airlineBaseEndpoint.post
    .in(jsonBody[AirlineInput])
    .errorOut(stringBody)
    .out(jsonBody[Airline])

  // PUT /airline?id=uuid with JSON Airline body
  val putAirline = airlineBaseEndpoint.put
    .in(query[UUID]("id"))
    .in(jsonBody[AirlineInput])
    .mapIn(e => PutAirlineInput(e._1, e._2))(pai => (pai.id, pai.airlineInput))
    .errorOut(stringBody)
    .out(jsonBody[Airline])

  // DELETE /airline?id=uuid
  val deleteAirline = airlineBaseEndpoint.delete
    .in(query[UUID]("id"))
    .out(jsonBody[UUID])
    .errorOut(stringBody)

  // GET /airlines?limit=m&skip=n&search=string
  val airlineListing = airlineBaseEndpoint.get
    .in("airlines")
    .in(query[Option[Int]]("limit"))
    .in(query[Option[Int]]("skip"))
    .in(query[String]("search"))
    .mapIn(data => AirlineListingInput(data._1, data._2, data._3))(ali => (ali.limit, ali.skip, ali.search))
    .out(jsonBody[List[Airline]])
    .errorOut(stringBody)

  def endpoints = List(
    postAirline,
    getAirline,
    deleteAirline,
    airlineListing
  )


  def swaggerEndpoints = SwaggerInterpreter()
    .fromEndpoints[Future](
      endpoints,
      "Couchbase airline API",
      "1.0"
    )
}
