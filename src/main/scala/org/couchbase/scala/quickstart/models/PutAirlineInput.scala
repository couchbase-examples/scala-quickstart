package org.couchbase.scala.quickstart.models

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{EndpointInput, query}

import java.util.UUID

case class PutAirlineInput(
                            id: UUID,
                            airlineInput: AirlineInput
                           )
