package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class Http4sConfig(port: Int)

object Http4sConfig {
  implicit val Http4sConfigReader: ConfigReader[Http4sConfig] =
    deriveReader[Http4sConfig]
}
