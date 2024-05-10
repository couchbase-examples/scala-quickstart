package org.couchbase.scala.quickstart.models

import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

final case class AirlineInput(
                               name: String,
                               country: String,
                               callsign: String,
                               iata: String,
                               icao: String
)

object AirlineInput {
  import io.circe.generic.semiauto._
  implicit val airlineInputDecoder: Decoder[AirlineInput] = deriveDecoder
  implicit val airlineInputEncoder: Encoder[AirlineInput] = deriveEncoder
  implicit val airlineInputSchema: Schema[AirlineInput] = Schema.derived
}
