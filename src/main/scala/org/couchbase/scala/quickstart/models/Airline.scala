package org.couchbase.scala.quickstart.models

import io.circe.{Decoder, Encoder, KeyEncoder}
import sttp.tapir.Schema

import java.util.UUID
import scala.util.Try

final case class Airline(
                        id: UUID,
                        name: String,
                        country: String,
                        callsign: String,
                        iata: String,
                        icao: String
)

object Airline {
  import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  implicit val airlineKeyEncoder: KeyEncoder[Airline] = KeyEncoder.instance(_.id.toString)
  implicit val airlineDecoder: Decoder[Airline] = deriveDecoder[Airline]
  implicit val airlineEncoder: Encoder[Airline] = deriveEncoder[Airline]
  implicit val airlineSchema: Schema[Airline] = Schema.derived
  def apply(
             id: String,
             name: String,
             country: String,
             callsign: String,
             iata: String,
             icao: String
  ): Try[Airline] = {
    for {
      airline <- Try {
        new Airline(UUID.fromString(id), name, country, callsign, iata, icao)
      }
    } yield airline
  }

  def fromAirlineInput(airlineInput: AirlineInput): Try[Airline] = {
    airlineInput match {
      case AirlineInput(name, country, callsign, iata, icao) => Airline(UUID.randomUUID.toString, name, country, callsign, iata, icao)
    }
  }
}