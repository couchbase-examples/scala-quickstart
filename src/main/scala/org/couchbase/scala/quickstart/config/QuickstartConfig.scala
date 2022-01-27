package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class QuickstartConfig(
    couchbase: CouchbaseConfig,
    akkaHttp: Option[AkkaHttpConfig],
    http4s: Option[Http4sConfig],
    play: Option[PlayConfig]
)

object QuickstartConfig {
  implicit val quickstartConfigReader: ConfigReader[QuickstartConfig] =
    deriveReader[QuickstartConfig]
}
