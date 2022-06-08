package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.config.GcpConfig
import io.fitcentive.user.domain.config.AppPubSubConfig
import io.fitcentive.user.infrastructure.settings.AppConfigService

// todo - individual service accounts
@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def gcpConfig: GcpConfig
  def pubSubConfig: AppPubSubConfig
}
