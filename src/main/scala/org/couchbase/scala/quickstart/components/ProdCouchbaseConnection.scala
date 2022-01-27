package org.couchbase.scala.quickstart.components

import com.couchbase.client.core.msg.kv.DurabilityLevel
import com.couchbase.client.scala.manager.bucket.CreateBucketSettings
import com.couchbase.client.scala.{Bucket, Cluster}
import org.couchbase.scala.quickstart.config.QuickstartConfig

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
      _ <- Future.fromTry(
        cl.buckets
          .getBucket(quickstartConfig.couchbase.bucketName)
          .recoverWith { case _ => cl.buckets.create(bucketSettings) }
      )
      _ <- Future.fromTry(cl.waitUntilReady(30.seconds))
    } yield cl.bucket(quickstartConfig.couchbase.bucketName)
  }
}
