package org.couchbase.scala.quickstart.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, KeyEncoder}
import sttp.tapir.Schema

import java.util.UUID

final case class Location(
                           lat: Double,
                           lon: Double,
                           accuracy: String
                         )

final case class Hotel(
                        id: Int,
                        title: String,
                        name: String,
                        address: String,
                        directions: String,
                        phone: String,
                        tollfree: String,
                        email: String,
                        fax: String,
                        url: String,
                        checkin: String,
                        checkout: String,
                        price: String,
                        geo: Location,
                        country: String,
                        city: String,
                        state: String,
                        vacancy: Boolean,
                        description: String,
                        alias: String,
                        pets_ok: Boolean,
                        free_breakfast: Boolean,
                        free_internet: Boolean,
                        free_parking: Boolean
) extends Model

object Location {
  implicit val decoder: Decoder[Location] = deriveDecoder[Location]
  implicit val encoder: Encoder.AsObject[Location] = deriveEncoder[Location]
  implicit val schema: Schema[Location] = Schema.derived[Location]
}

object Hotel {
  implicit val keyEncoder: KeyEncoder[Hotel] = KeyEncoder.instance(_.id.toString)
  implicit val decoder: Decoder[Hotel] = deriveDecoder[Hotel]
  implicit val encoder: Encoder.AsObject[Hotel] = deriveEncoder[Hotel]
  implicit val schema: Schema[Hotel] = Schema.derived[Hotel]
}
