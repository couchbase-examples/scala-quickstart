package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.query.{QueryOptions, QueryScanConsistency}
import io.circe.{Decoder, Encoder}
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.models.CirceGetResult._
import org.couchbase.scala.quickstart.models.{Airline, ListingInput}
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{AnyEndpoint, Schema, query, stringBody}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

class CouchbaseAirlineController(
                                  implicit val couchbaseConnection: ProdCouchbaseConnection,
                                  implicit val quickstartConfig: QuickstartConfig,
                                ) extends ListingController[Airline] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val collection: Future[AsyncCollection] =
    couchbaseConnection.bucket.map(
      _.async.scope(quickstartConfig.couchbase.scopeName).collection("airline")
    )

  override def get(id: String): Future[Either[String, Airline]] = {
    println("Fetching airline: " + id)
    for {
      ac <- collection
      res <- ac.get(id).map(_.contentAsCirceJson[Airline]).
        recover { case _: DocumentNotFoundException => Left(s"Could not retrieve Airline. ID: $id was not found.")}
    } yield res
  }

  override def post(
                     airline: Airline
                   ): Future[Either[String, Airline]] = {
    import io.circe.syntax._
    for {
      ac <- collection
      result <- ac.insert[io.circe.Json](airline.id.toString, airline.asJson)
        .map(_ => Right(airline))
        .recover { case ex: Exception => Left(ex.getMessage)}
    } yield result
  }

  override def delete(id: String): Future[Either[String, String]] = {
    for {
      ac <- collection
      removeResult <- ac.remove(id).map(_ => id).recover {
        case NonFatal(t) => t.getMessage
      }
    } yield Right(removeResult)
  }

  override def list(args: ListingInput): Future[Either[String, List[Airline]]] = {
    val query = s"SELECT a.* FROM " +
      s"`${quickstartConfig.couchbase.bucketName}`.`${quickstartConfig.couchbase.scopeName}`.`airline` a " +
      s"WHERE lower(a.name) LIKE '%${args.search.toLowerCase}%' " +
      s"OR lower(a.country) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.callsign) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.iata) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.icao) LIKE '%${args.search.toLowerCase}%'  " +
      s"LIMIT " + args.limit.getOrElse(5) + " OFFSET " + args.skip.getOrElse(0)

    import cats.implicits._
    for {
      cluster <- couchbaseConnection.cluster
      rows <- Future.fromTry(
        cluster.query(
          query,
          QueryOptions(scanConsistency =
            Some(QueryScanConsistency.RequestPlus())
          )
        )
      )
      airlines <- Future.fromTry(
        rows
          .rowsAs[io.circe.Json]
          .map(_.map(json => json.as[Airline].left.map(_.getMessage())))
      )
      accumulatedAirlines = airlines.toList.sequence
    } yield accumulatedAirlines
  }

  override def endpoints(): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = super.endpointDefs()(
    Airline.encoder, Airline.decoder, Airline.schema
  )

  override def modelType() = classOf[Airline]
}
