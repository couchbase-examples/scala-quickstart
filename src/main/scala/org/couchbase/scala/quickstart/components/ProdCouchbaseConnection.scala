package org.couchbase.scala.quickstart.components

import com.couchbase.client.scala.manager.bucket.CreateBucketSettings
import com.couchbase.client.scala.{Bucket, Cluster}
import org.couchbase.scala.quickstart.config.QuickstartConfig

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ProdCouchbaseConnection(quickstartConfig: QuickstartConfig)
    extends CouchbaseConnection[Future] {
  implicit val ec: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  override lazy val cluster: Future[Cluster] = Future.fromTry(
    Cluster.connect(
      quickstartConfig.couchbase.host,
      quickstartConfig.couchbase.username,
      quickstartConfig.couchbase.password
    )
  )

  override lazy val bucket: Future[Bucket] = {
    val bucketSettings = CreateBucketSettings(
      quickstartConfig.couchbase.bucketName,
      quickstartConfig.couchbase.bucketSize
    )

    for {
      cl <- cluster
      // Try to get the bucket, and if it fails, create it instead.
      _ <- Future.fromTry(
        cl.buckets
          .getBucket(quickstartConfig.couchbase.bucketName)
          .recoverWith { case NonFatal(_) => cl.buckets.create(bucketSettings) }
      )
      // Buckets are created async, so we block here instead.
      _ <- Future.fromTry(cl.waitUntilReady(30.seconds))
    } yield cl.bucket(quickstartConfig.couchbase.bucketName)
  }
}
