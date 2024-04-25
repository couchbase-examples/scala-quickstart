package org.couchbase.scala.quickstart.models

import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{EndpointInput, query}

import java.util.UUID

case class PutProfileInput (
                           pid: UUID,
                           profileInput: ProfileInput
                           )
