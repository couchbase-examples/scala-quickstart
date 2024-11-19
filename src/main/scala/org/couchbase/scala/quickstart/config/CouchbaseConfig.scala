package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class CouchbaseConfig(
    host: String,
    username: String,
    password: String,
    bucketName: String,
    bucketSize: Int,
    scopeName: String,
    collectionName: String,
)

object CouchbaseConfig {
  implicit val couchbaseConfigReader: ConfigReader[CouchbaseConfig] =
    deriveReader[CouchbaseConfig]
}
