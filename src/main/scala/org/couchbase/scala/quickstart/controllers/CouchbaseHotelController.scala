package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.query.{QueryOptions, QueryScanConsistency}
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.models.CirceGetResult._
import org.couchbase.scala.quickstart.models.{Hotel, ListingInput}
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal
import io.circe._
import io.circe.parser._

class CouchbaseHotelController(
    implicit val couchbaseConnection: ProdCouchbaseConnection,
    implicit val quickstartConfig: QuickstartConfig,
) extends ListingController[Hotel] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val collection: Future[AsyncCollection] =
    couchbaseConnection.bucket.map(
      _.async.scope(quickstartConfig.couchbase.scopeName).collection("hotel")
    )

  override def get(id: String): Future[Either[String, Hotel]] = {
    for {
      ac <- collection
      res <- ac.get(id).map(_.contentAsCirceJson[Hotel]).
        recover { case _: DocumentNotFoundException => Left(s"Could not retrieve Hotel. ID: $id was not found.")}
    } yield res
  }

  override def post(
                            hotel: Hotel
  ): Future[Either[String, Hotel]] = {
    import io.circe.syntax._
    for {
      ac <- collection
      result <- ac.insert[io.circe.Json](hotel.id.toString, hotel.asJson)
        .map(_ => Right(hotel))
        .recover { case ex: Exception => Left(ex.getMessage)}
    } yield result
  }

  override def delete(id: String): Future[Either[String, String]] = {
    for{
      ac <- collection
      removeResult <- ac.remove(id).map(_ => id).recover {
        case NonFatal(t) => t.getMessage
      }
    } yield Right(removeResult)
  }

  override def list(args: ListingInput): Future[Either[String, List[Hotel]]] = {
    val query = s"SELECT p.* FROM " +
      s"`${quickstartConfig.couchbase.bucketName}`.`${quickstartConfig.couchbase.scopeName}`.`hotel` a " +
      s"WHERE lower(a.name) LIKE '%${args.search.toLowerCase}%' " +
      s"OR lower(a.description) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.title) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.country) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.state) LIKE '%${args.search.toLowerCase}%'  " +
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
      hotels <- Future.fromTry(
        rows
          .rowsAs[io.circe.Json]
          .map(_.map(json => json.as[Hotel].left.map(_.getMessage())))
      )
      accumulatedHotels = hotels.toList.sequence
    } yield accumulatedHotels
  }

  override def endpoints(): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = super.endpointDefs()(
    Hotel.encoder, Hotel.decoder, Hotel.schema
  )

  override def modelType() = classOf[Hotel]
}
