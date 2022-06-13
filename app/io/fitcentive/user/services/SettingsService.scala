package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.config.{GcpConfig, JwtConfig, SecretConfig, ServerConfig}
import io.fitcentive.user.domain.config.AppPubSubConfig
import io.fitcentive.user.infrastructure.settings.AppConfigService

// todo - individual service accounts
@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def gcpConfig: GcpConfig
  def pubSubConfig: AppPubSubConfig
  def authServiceConfig: ServerConfig
  def jwtConfig: JwtConfig
  def keycloakServerUrl: String
  def secretConfig: SecretConfig
}
