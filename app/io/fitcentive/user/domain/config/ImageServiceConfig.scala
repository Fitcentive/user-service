package io.fitcentive.user.domain.config

import com.typesafe.config.Config

case class ImageServiceConfig(host: String, port: String, token: String) {
  val serverUrl: String = s"$host:$port"
}

object ImageServiceConfig {
  def fromConfig(config: Config): ImageServiceConfig =
    ImageServiceConfig(
      host = config.getString("host"),
      port = config.getString("port"),
      token = config.getString("token")
    )
}
