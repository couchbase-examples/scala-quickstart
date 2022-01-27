package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class AkkaHttpConfig(port: Int)

object AkkaHttpConfig {
  implicit val akkaHttpConfigReader: ConfigReader[AkkaHttpConfig] =
    deriveReader[AkkaHttpConfig]
}
