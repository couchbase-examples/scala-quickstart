package org.couchbase.scala.quickstart.controllers

import com.couchbase.client.core.error.{CollectionExistsException, DocumentNotFoundException, IndexExistsException}
import com.couchbase.client.scala.AsyncCollection
import com.couchbase.client.scala.manager.collection.CollectionSpec
import com.couchbase.client.scala.query.{QueryOptions, QueryScanConsistency}
import org.couchbase.scala.quickstart.Endpoints
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.models.CirceGetResult._
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput, ProfileListingInput, PutProfileInput}
import sttp.tapir.endpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.NettyFutureServer

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class CouchbaseProfileController(
    couchbaseConnection: ProdCouchbaseConnection,
    quickstartConfig: QuickstartConfig,
) extends ProfileController[Future] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val profileCollection: Future[AsyncCollection] =
    couchbaseConnection.bucket.map(
      _.async.collection(quickstartConfig.couchbase.collectionName)
    )

  override def getProfile(pid: UUID): Future[Either[String, Profile]] = {
    for {
      pc <- profileCollection
      res <- pc.get(pid.toString).map(_.contentAsCirceJson[Profile]).
        recover { case _: DocumentNotFoundException => Left(s"Could not retrieve Profile. ID: $pid was not found.")}
    } yield res
  }

  override def postProfile(
      profileInput: ProfileInput
  ): Future[Either[String, Profile]] = {
    import io.circe.syntax._
    for {
      pc <- profileCollection
      profile <- Profile.fromProfileInput(profileInput) match {
        case Failure(exception) => Future.successful(Left(exception.toString))
        case Success(p) =>
          pc.insert[io.circe.Json](p.pid.toString, p.asJson) map (_ => Right(p))
      }
    } yield profile
  }

  override def putProfile(
                         args: PutProfileInput
  ): Future[Either[String, Profile]] = {
    import io.circe.syntax._
    for {
      pc <- profileCollection
      profile <- Profile
        .fromProfileInput(args.profileInput)
        // Replace the newly generated PID with the old PID:
        .map(_.copy(pid = args.pid)) match {
        case Failure(exception) => Future.successful(Left(exception.toString))
        case Success(p) =>
          pc.upsert[io.circe.Json](args.pid.toString, p.asJson) map (_ => Right(p))
      }
    } yield profile
  }

  override def deleteProfile(pid: UUID): Future[Either[String, UUID]] = {
    for {
      pc <- profileCollection
      removeResult <- pc.remove(pid.toString).map(_ => Right(pid)).recover {
        case NonFatal(t) => Left(t.getMessage)
      }
    } yield removeResult
  }

  override def profileListing(args: ProfileListingInput): Future[Either[String, List[Profile]]] = {
    val query = s"SELECT p.* FROM " +
      s"`${quickstartConfig.couchbase.bucketName}`.`_default`.`${quickstartConfig.couchbase.collectionName}` p " +
      s"WHERE lower(p.firstName) LIKE '%${args.search.toLowerCase}%' " +
      s"OR lower(p.lastName) LIKE '%${args.search.toLowerCase}%'  " +
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
      profiles <- Future.fromTry(
        rows
          .rowsAs[io.circe.Json]
          .map(_.map(json => json.as[Profile].left.map(_.getMessage())))
      )
      accumulatedProfiles = profiles.toList.sequence
    } yield accumulatedProfiles
  }

  def setupIndexesAndCollections() = {
    for {
      _ <- createPrimaryIndex()
      _ <- createCollection()
      // Wait until the collection is actually created.
      _ <- Future { Thread.sleep(5000L) }
      qr <- createCollectionIndexes()
    } yield qr
  }

  def createPrimaryIndex() = {
    for {
      cluster <- couchbaseConnection.cluster
      indexResult <- Future.fromTry {
        cluster.queryIndexes
          .createPrimaryIndex(quickstartConfig.couchbase.bucketName)
          .recover { case _: IndexExistsException => () }
      }
    } yield indexResult
  }

  def createCollection() = {
    for {
      bucket <- couchbaseConnection.bucket
      collectionManager = bucket.collections
      collectionSpec = CollectionSpec(
        quickstartConfig.couchbase.collectionName,
        bucket.defaultScope.name
      )
      // Try to create a new collection. This is also counted as successful if the collection already exists.
      collection <- Future.fromTry {
        collectionManager
          .createCollection(collectionSpec)
          .recoverWith { case _: CollectionExistsException => Success(()) }
      }
    } yield collection
  }

  def createCollectionIndexes() = {
    val query = s"CREATE PRIMARY INDEX default_profile_index ON " +
      s"${quickstartConfig.couchbase.bucketName}._default.${quickstartConfig.couchbase.collectionName}"

    for {
      cluster <- couchbaseConnection.cluster
      cq <- Future.fromTry(cluster.query(query).recoverWith {
        case _: IndexExistsException => Success(())
      })
    } yield cq
  }
}
