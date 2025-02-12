package org.couchbase.scala.quickstart.controllers

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.couchbase.scala.quickstart.models.{ListingInput, Model}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait ListingController[T <: Model] extends ModelController[T] {
  def list(input: ListingInput): Future[Either[String, List[T]]]

  override protected def endpointDefs()(
    encoder: Encoder[T],
    decoder: Decoder[T],
    schema: Schema[T],
  ): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = {

    val endpoints = super.endpointDefs()(encoder, decoder, schema)
    val ep = base().get
      .in("list")
      .in(query[Option[Int]]("limit"))
      .in(query[Option[Int]]("skip"))
      .in(query[String]("search"))
      .out(jsonBody[List[T]](Encoder.encodeList[T](encoder), Decoder.decodeList[T](decoder), Schema.schemaForIterable(schema)))
      .errorOut(stringBody)
      .mapIn(data => ListingInput(data._1, data._2, data._3))(li => (li.limit, li.skip, li.search))

    (endpoints._1.appended(ep), endpoints._2.appended(ep.serverLogic(this.list _)))
  }

}
