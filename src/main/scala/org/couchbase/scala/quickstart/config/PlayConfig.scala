package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class PlayConfig(port: Int)

object PlayConfig {
  implicit val playReader: ConfigReader[PlayConfig] = deriveReader[PlayConfig]
}
