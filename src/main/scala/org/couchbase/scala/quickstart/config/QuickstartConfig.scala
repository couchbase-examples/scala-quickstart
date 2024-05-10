package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class QuickstartConfig(
    couchbase: CouchbaseConfig,
    netty: NettyConfig,
)

object QuickstartConfig {
  implicit val quickstartConfigReader: ConfigReader[QuickstartConfig] =
    deriveReader[QuickstartConfig]
}
