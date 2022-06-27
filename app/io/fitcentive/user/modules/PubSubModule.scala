package io.fitcentive.user.modules

import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.sdk.gcp.pubsub.PubSubPublisher
import io.fitcentive.user.infrastructure.pubsub.SubscriptionManager
import io.fitcentive.user.modules.providers.SubscriptionManagerProvider
import io.fitcentive.user.services.SettingsService

import javax.inject.Singleton

class PubSubModule extends AbstractModule {

  @Provides
  @Singleton
  def provideGcpCredentials: Credentials =
    ServiceAccountCredentials
      .fromStream(getClass.getResourceAsStream("/fitcentive-1210-8be2af66c953.json"))
      .createScoped()

  @Provides
  @Singleton
  def providePubSubPublisher(settingsService: SettingsService): PubSubPublisher =
    new PubSubPublisher(settingsService.gcpConfig.credentials, settingsService.gcpConfig.project)

  override def configure(): Unit = {
    bind(classOf[SubscriptionManager]).toProvider(classOf[SubscriptionManagerProvider]).asEagerSingleton()
  }

}
