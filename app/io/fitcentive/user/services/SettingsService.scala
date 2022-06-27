package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.config.{GcpConfig, JwtConfig, SecretConfig, ServerConfig}
import io.fitcentive.user.domain.config.{AppPubSubConfig, EnvironmentConfig, ImageServiceConfig}
import io.fitcentive.user.infrastructure.settings.AppConfigService

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def gcpConfig: GcpConfig
  def pubSubConfig: AppPubSubConfig
  def authServiceConfig: ServerConfig
  def imageServiceConfig: ImageServiceConfig
  def jwtConfig: JwtConfig
  def keycloakServerUrl: String
  def secretConfig: SecretConfig
  def envConfig: EnvironmentConfig
}
