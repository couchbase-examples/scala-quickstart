package org.couchbase.scala.quickstart.models

import io.circe.{Decoder, Encoder, KeyEncoder}
import sttp.tapir.Schema

import java.util.UUID
import scala.util.Try

final case class Airport(
                        id: Int,
                        airportname: String,
                        city: String,
                        country: String,
                        faa: String,
                        icao: String,
                        tz: String
) extends Model

object Airport {
  import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  implicit val keyEncoder: KeyEncoder[Airport] = KeyEncoder.instance(_.id.toString)
  implicit val decoder: Decoder[Airport] = deriveDecoder[Airport]
  implicit val encoder: Encoder.AsObject[Airport] = deriveEncoder[Airport]
  implicit val schema: Schema[Airport] = Schema.derived[Airport]
}