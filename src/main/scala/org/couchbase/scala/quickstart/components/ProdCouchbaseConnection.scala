package org.couchbase.scala.quickstart.components

import com.couchbase.client.core.deps.io.netty.handler.ssl.util.InsecureTrustManagerFactory
import com.couchbase.client.scala.env.{
  ClusterEnvironment,
  PasswordAuthenticator,
  SecurityConfig
}
import com.couchbase.client.scala.manager.bucket.CreateBucketSettings
import com.couchbase.client.scala.{Bucket, Cluster, ClusterOptions}
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
      // Try to get the bucket, and if it fails, log an error and exit.
      _ <- Future.fromTry(
        cl.buckets
          .getBucket(quickstartConfig.couchbase.bucketName)
          .recoverWith { case NonFatal(_) => {
            System.err.println(String.format("Error: unable to find bucket '%s'", quickstartConfig.couchbase.bucketName))
            System.exit(1)
            null
          } }
      )
      // Buckets are created async, so we block here instead.
      _ <- Future.fromTry(cl.waitUntilReady(30.seconds))
    } yield cl.bucket(quickstartConfig.couchbase.bucketName)
  }
}
