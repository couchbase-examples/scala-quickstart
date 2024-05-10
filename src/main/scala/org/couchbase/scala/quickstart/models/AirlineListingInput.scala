package org.couchbase.scala.quickstart.models

import sttp.tapir.{EndpointInput, query}

case class AirlineListingInput(
                              limit: Option[Int],
                              skip: Option[Int],
                              search: String,
                              )

