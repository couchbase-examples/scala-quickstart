package org.couchbase.scala.quickstart.components

import com.couchbase.client.scala.{Bucket, Cluster}

trait CouchbaseConnection[F[_]] {
  val cluster: F[Cluster]
  val bucket: F[Bucket]
}
