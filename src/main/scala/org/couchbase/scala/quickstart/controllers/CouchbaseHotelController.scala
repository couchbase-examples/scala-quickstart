package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.AsyncCollection
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
    val query = ListingQuery(
      quickstartConfig.couchbase.bucketName,
      quickstartConfig.couchbase.scopeName,
      "hotel",
      Seq("name", "description", "title", "country", "state", "faa", "city", "icao"),
      args
    )
    couchbaseConnection.cluster.flatMap(ListingQuery.run[Hotel](_, query))
  }

  override def endpoints(): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = super.endpointDefs()(
    Hotel.encoder, Hotel.decoder, Hotel.schema
  )

  override def modelType() = classOf[Hotel]
}
