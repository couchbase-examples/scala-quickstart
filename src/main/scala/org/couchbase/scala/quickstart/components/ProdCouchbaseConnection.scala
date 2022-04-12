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
    if (!quickstartConfig.couchbase.capella)
      Cluster.connect(
        quickstartConfig.couchbase.host,
        quickstartConfig.couchbase.username,
        quickstartConfig.couchbase.password
      )
    else {
      for {
        clusterEnv <- ClusterEnvironment.builder
          .securityConfig(
            SecurityConfig()
              .enableTls(true)
              // Disable certificate checking. This should not be used in production!
              .trustManagerFactory(InsecureTrustManagerFactory.INSTANCE)
          )
          .build
        clusterOptions = ClusterOptions(
          PasswordAuthenticator(
            quickstartConfig.couchbase.username,
            quickstartConfig.couchbase.password
          ),
          Some(clusterEnv)
        )
        cl <- Cluster.connect(quickstartConfig.couchbase.host, clusterOptions)
      } yield cl
    }
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
          // Note: at the moment it will fail to create a bucket with Capella enabled. Instead, the user will need to
          // create it via the Capella user interface.
          .recoverWith { case NonFatal(_) => cl.buckets.create(bucketSettings) }
      )
      // Buckets are created async, so we block here instead.
      _ <- Future.fromTry(cl.waitUntilReady(30.seconds))
    } yield cl.bucket(quickstartConfig.couchbase.bucketName)
  }
}
