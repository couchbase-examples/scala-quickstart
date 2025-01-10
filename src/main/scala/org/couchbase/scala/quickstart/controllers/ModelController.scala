package org.couchbase.scala.quickstart.controllers

import io.circe.{Decoder, Encoder, KeyEncoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.couchbase.scala.quickstart.models.{Airline, ListingInput, Model}
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future
import scala.reflect.ClassTag

trait ModelController[T <: Model] {
  def get(id: String): Future[Either[String, T]]

  def post(input: T): Future[Either[String, T]]

  def delete(id: String): Future[Either[String, String]]

  def modelType(): Class[T]

  def base() = endpoint.in("api" / "v1" / modelType().getSimpleName().toLowerCase())

  protected def endpointDefs()(
    encoder: Encoder[T],
    decoder: Decoder[T],
    schema: Schema[T],
  ): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = {

    val getEndpoint = base().get
      .in(query[String]("id"))
      .out(jsonBody[T](encoder, decoder, schema))
      .errorOut(stringBody)

    val postEndpoint = base().post
      .in(jsonBody[T](encoder, decoder, schema))
      .out(jsonBody[T](encoder, decoder, schema))
      .errorOut(stringBody)

    val deleteEndpoint = base().delete
      .in(query[String]("id"))
      .out(stringBody)
      .errorOut(stringBody)


    val endpoints = List[AnyEndpoint](
      getEndpoint, postEndpoint, deleteEndpoint
    )

    val logic = List[ServerEndpoint[Any, Future]](
      getEndpoint.serverLogic(this.get _),
      postEndpoint.serverLogic(this.post _),
      deleteEndpoint.serverLogic(this.delete _)
    )
    (endpoints, logic)
  }

  def endpoints(): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]])
}

object ModelController {
}
