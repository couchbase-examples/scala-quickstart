package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.codec.JsonSerializer
import io.circe.generic.auto._
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.models.CirceGetResult._
import org.couchbase.scala.quickstart.models.Route
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}
import scala.util.control.NonFatal

class CouchbaseRouteController(
    implicit val couchbaseConnection: ProdCouchbaseConnection,
    implicit val quickstartConfig: QuickstartConfig,
) extends ModelController[Route] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val collection: Future[AsyncCollection] =
    couchbaseConnection.bucket.map(
      _.async.scope(quickstartConfig.couchbase.scopeName).collection("route")
    )

  override def get(id: String): Future[Either[String, Route]] = {
    for {
      ac <- collection
      res <- ac.get(id).map(_.contentAsCirceJson[Route]).
        recover { case _: DocumentNotFoundException => Left(s"Could not retrieve Route. ID: $id was not found.")}
    } yield res
  }

  override def post(
                            route: Route
  ): Future[Either[String, Route]] = {
    import io.circe.syntax._
    for {
      ac <- collection
      result <- ac.insert[io.circe.Json](route.id.toString, route.asJson)
        .map(_ => Right(route))
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

  override def modelType() = classOf[Route]

  override def endpoints(): (List[AnyEndpoint], List[ServerEndpoint[Any, Future]]) = endpointDefs()(
    Route.encoder, Route.decoder, Route.schema
  )
}
