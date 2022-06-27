package io.fitcentive.user.modules.providers

import io.fitcentive.sdk.gcp.pubsub.{PubSubPublisher, PubSubSubscriber}
import io.fitcentive.user.api.UserApi
import io.fitcentive.user.infrastructure.contexts.PubSubExecutionContext
import io.fitcentive.user.infrastructure.handlers.MessageEventHandlers
import io.fitcentive.user.infrastructure.pubsub.SubscriptionManager
import io.fitcentive.user.services.SettingsService

import javax.inject.{Inject, Provider}
import scala.concurrent.ExecutionContext

class SubscriptionManagerProvider @Inject() (
  publisher: PubSubPublisher,
  settingsService: SettingsService,
  _userApi: UserApi
)(implicit ec: PubSubExecutionContext)
  extends Provider[SubscriptionManager] {

  trait SubscriptionEventHandlers extends MessageEventHandlers {
    override def userApi: UserApi = _userApi
    override implicit def executionContext: ExecutionContext = ec
  }

  override def get(): SubscriptionManager = {
    new SubscriptionManager(
      publisher = publisher,
      subscriber = new PubSubSubscriber(settingsService.gcpConfig.credentials, settingsService.gcpConfig.project),
      config = settingsService.pubSubConfig,
      environment = settingsService.envConfig.environment
    ) with SubscriptionEventHandlers
  }
}
