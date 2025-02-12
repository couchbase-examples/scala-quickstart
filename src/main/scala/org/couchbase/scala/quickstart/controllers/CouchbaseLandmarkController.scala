package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.query.{QueryOptions, QueryScanConsistency}
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.models.CirceGetResult._
import org.couchbase.scala.quickstart.models.{Landmark, ListingInput}
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

class CouchbaseLandmarkController(
    implicit val couchbaseConnection: ProdCouchbaseConnection,
    implicit val quickstartConfig: QuickstartConfig,
) extends ListingController[Landmark] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val collection: Future[AsyncCollection] =
    couchbaseConnection.bucket.map(
      _.async.scope(quickstartConfig.couchbase.scopeName).collection("landmark")
    )

  override def get(id: String): Future[Either[String, Landmark]] = {
    for {
      ac <- collection
      res <- ac.get(id).map(_.contentAsCirceJson[Landmark]).
        recover { case _: DocumentNotFoundException => Left(s"Could not retrieve Landmark. ID: $id was not found.")}
    } yield res
  }

  override def post(
                            landmark: Landmark
  ): Future[Either[String, Landmark]] = {
    import io.circe.syntax._
    for {
      ac <- collection
      result <- ac.insert[io.circe.Json](landmark.id.toString, landmark.asJson)
        .map(_ => Right(landmark))
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

  override def list(args: ListingInput): Future[Either[String, List[Landmark]]] = {
    val query = s"SELECT p.* FROM " +
      s"`${quickstartConfig.couchbase.bucketName}`.`${quickstartConfig.couchbase.scopeName}`.`landmark` a " +
      s"WHERE lower(a.name) LIKE '%${args.search.toLowerCase}%' " +
      s"OR lower(a.content) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.title) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.country) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.state) LIKE '%${args.search.toLowerCase}%'  " +
      s"OR lower(a.city) LIKE '%${args.search.toLowerCase}%'  " +
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
      landmarks <- Future.fromTry(
        rows
          .rowsAs[io.circe.Json]
          .map(_.map(json => json.as[Landmark].left.map(_.getMessage())))
      )
      accumulatedLandmarks = landmarks.toList.sequence
    } yield accumulatedLandmarks
  }

  override def modelType() = classOf[Landmark]

  override def endpoints(): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = endpointDefs()(
    Landmark.encoder, Landmark.decoder, Landmark.schema
  )
}
