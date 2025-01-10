package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.query.{QueryOptions, QueryScanConsistency}
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.models.CirceGetResult._
import org.couchbase.scala.quickstart.models.{Airport, ListingInput}
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

class CouchbaseAirportController(
    implicit val couchbaseConnection: ProdCouchbaseConnection,
    implicit val quickstartConfig: QuickstartConfig,
) extends ListingController[Airport] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val collection: Future[AsyncCollection] =
    couchbaseConnection.bucket.map(
      _.async.scope(quickstartConfig.couchbase.scopeName).collection("airport")
    )

  override def get(id: String): Future[Either[String, Airport]] = {
    for {
      ac <- collection
      res <- ac.get(id).map(fetch => fetch.contentAsCirceJson[Airport]).
        recover { case _: DocumentNotFoundException => Left(s"Could not retrieve Airport. ID: $id was not found.")}
    } yield res
  }

  override def post(
                            airport: Airport
  ): Future[Either[String, Airport]] = {
    import io.circe.syntax._
    for {
      ac <- collection
      result <- ac.insert[io.circe.Json](airport.id.toString, airport.asJson)
        .map(_ => Right(airport))
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

  override def list(args: ListingInput): Future[Either[String, List[Airport]]] = {
    val query = s"SELECT p.* FROM " +
      s"`${quickstartConfig.couchbase.bucketName}`.`${quickstartConfig.couchbase.scopeName}`.`airport` a " +
      s"WHERE lower(a.airportname) LIKE '%${args.search.toLowerCase}%' " +
      s"OR lower(a.country) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.faa) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.city) LIKE '%${args.search.toLowerCase}%'  " +
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
      airports <- Future.fromTry(
        rows
          .rowsAs[io.circe.Json]
          .map(_.map(json => json.as[Airport].left.map(_.getMessage())))
      )
      accumulatedAirports = airports.toList.sequence
    } yield accumulatedAirports
  }

  override def endpoints(): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = super.endpointDefs()(
    Airport.encoder, Airport.decoder, Airport.schema
  )

  override def modelType() = classOf[Airport]
}