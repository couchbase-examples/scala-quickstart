package org.couchbase.scala.quickstart.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class NettyConfig(port: Int)

object NettyConfig {
  implicit val NettyConfigReader: ConfigReader[NettyConfig] = deriveReader[NettyConfig]
}