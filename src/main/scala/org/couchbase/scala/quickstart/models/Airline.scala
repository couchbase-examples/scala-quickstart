package org.couchbase.scala.quickstart.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, KeyEncoder}
import sttp.tapir.Schema

final case class Airline (
                        id: Int,
                        name: String,
                        country: String,
                        callsign: String,
                        iata: String,
                        icao: String
) extends Model {
}

object Airline {
  implicit val keyEncoder: KeyEncoder[Airline] = KeyEncoder.instance(_.id.toString)
  implicit val decoder: Decoder[Airline] = deriveDecoder[Airline]
  implicit val encoder: Encoder[Airline] = deriveEncoder[Airline]
  implicit val schema: Schema[Airline] = Schema.derived[Airline]
}