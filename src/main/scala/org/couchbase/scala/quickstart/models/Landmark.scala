package org.couchbase.scala.quickstart.models

import io.circe.{Decoder, Encoder, KeyEncoder}
import sttp.tapir.Schema

import java.util.UUID
import scala.util.Try

final case class Landmark(
                        id: Int,
                        title: String,
                        name: String,
                        alt: String,
                        address: String,
                        directions: String,
                        phone: String,
                        tollfree: String,
                        email: String,
                        url: String,
                        hours: String,
                        image: String,
                        price: String,
                        content: String,
                        geo: Location,
                        activity: String,
                        country: String,
                        city: String,
                        state: String
) extends Model

object Landmark{
  import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  implicit val keyEncoder: KeyEncoder[Landmark] = KeyEncoder.instance(_.id.toString)
  implicit val decoder: Decoder[Landmark] = deriveDecoder[Landmark]
  implicit val encoder: Encoder.AsObject[Landmark] = deriveEncoder[Landmark]
  implicit val schema: Schema[Landmark] = Schema.derived[Landmark]
}