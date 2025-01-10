package org.couchbase.scala.quickstart.models

import io.circe.{Decoder, Encoder, KeyEncoder}
import sttp.tapir.Schema

import java.util.UUID
import scala.concurrent.Future


trait Model {
  def id: Int
}