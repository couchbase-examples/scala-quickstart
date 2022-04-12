package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class CouchbaseConfig(
    host: String,
    username: String,
    password: String,
    bucketName: String,
    bucketSize: Int,
    collectionName: String,
    // When true, connect to Couchbase Capella (Cloud).
    capella: Boolean
)

object CouchbaseConfig {
  implicit val couchbaseConfigReader: ConfigReader[CouchbaseConfig] =
    deriveReader[CouchbaseConfig]
}
