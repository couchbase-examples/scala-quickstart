package org.couchbase.scala.quickstart.models

import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

final case class ProfileInput(
    firstName: String,
    lastName: String,
    email: String,
    password: String
)

object ProfileInput {
  import io.circe.generic.semiauto._
  implicit val profileInputDecoder: Decoder[ProfileInput] = deriveDecoder
  implicit val profileInputEncoder: Encoder[ProfileInput] = deriveEncoder
  implicit val profileInputSchema: Schema[ProfileInput] = Schema.derived
}
