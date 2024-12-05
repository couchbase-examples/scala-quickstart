package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.DocumentNotFoundException
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.query.{QueryOptions, QueryScanConsistency}
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.models.CirceGetResult._
import org.couchbase.scala.quickstart.models.{Airline, AirlineInput, AirlineListingInput, PutAirlineInput}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class CouchbaseAirlineController(
    couchbaseConnection: ProdCouchbaseConnection,
    quickstartConfig: QuickstartConfig,
) extends AirlineController[Future] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val airlineCollection: Future[AsyncCollection] =
    couchbaseConnection.bucket.map(
      _.async.scope(quickstartConfig.couchbase.scopeName).collection(quickstartConfig.couchbase.collectionName)
    )

  override def getAirline(id: UUID): Future[Either[String, Airline]] = {
    for {
      ac <- airlineCollection
      res <- ac.get(id.toString).map(_.contentAsCirceJson[Airline]).
        recover { case _: DocumentNotFoundException => Left(s"Could not retrieve Airline. ID: $id was not found.")}
    } yield res
  }

  override def postAirline(
                            airlineInput: AirlineInput
  ): Future[Either[String, Airline]] = {
    import io.circe.syntax._
    for {
      ac <- airlineCollection
      airline <- Airline.fromAirlineInput(airlineInput) match {
        case Failure(exception) => Future.successful(Left(exception.toString))
        case Success(a) =>
          ac.insert[io.circe.Json](a.id.toString, a.asJson) map (_ => Right(a))
      }
    } yield airline
  }

  override def putAirline(
                         args: PutAirlineInput
  ): Future[Either[String, Airline]] = {
    import io.circe.syntax._
    for {
      ac <- airlineCollection
      airline <- Airline
        .fromAirlineInput(args.airlineInput)
        // Replace the newly generated PID with the old PID:
        .map(_.copy(id = args.id)) match {
        case Failure(exception) => Future.successful(Left(exception.toString))
        case Success(p) =>
          ac.upsert[io.circe.Json](args.id.toString, p.asJson) map (_ => Right(p))
      }
    } yield airline
  }

  override def deleteAirline(id: UUID): Future[Either[String, UUID]] = {
    for {
      ac <- airlineCollection
      removeResult <- ac.remove(id.toString).map(_ => Right(id)).recover {
        case NonFatal(t) => Left(t.getMessage)
      }
    } yield removeResult
  }

  override def airlineListing(args: AirlineListingInput): Future[Either[String, List[Airline]]] = {
    val query = s"SELECT p.* FROM " +
      s"`${quickstartConfig.couchbase.bucketName}`.`${quickstartConfig.couchbase.scopeName}`.`${quickstartConfig.couchbase.collectionName}` a " +
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

}
