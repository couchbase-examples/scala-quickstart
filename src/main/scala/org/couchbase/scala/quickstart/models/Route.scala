package org.couchbase.scala.quickstart.models

import com.couchbase.client.core.deps.io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker13
import io.circe.{Decoder, Encoder, KeyEncoder}
import sttp.tapir.Schema

import java.util.UUID
import scala.util.Try


case class Schedule (
                            day: Int,
                            utc: String,
                            flight: String
                          )

final case class Route(
                        id: Int,
                        airline: String,
                        airlineid: String,
                        sourceairport: String,
                        destinationairport: String,
                        stops: Int,
                        equipment: String,
                        distance: Float,
                        schedule: List[Schedule]
) extends Model

object Schedule{
  import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  implicit val decoder: Decoder[Schedule] = deriveDecoder[Schedule]
  implicit val encoder: Encoder[Schedule] = deriveEncoder[Schedule]
  implicit val schema: Schema[Schedule] = Schema.derived
}
object Route{
  import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
  implicit val keyEncoder: KeyEncoder[Route] = KeyEncoder.instance(_.id.toString)
  implicit val decoder: Decoder[Route] = deriveDecoder[Route]
  implicit val encoder: Encoder[Route] = deriveEncoder[Route]
  implicit val schema: Schema[Route] = Schema.derived
}