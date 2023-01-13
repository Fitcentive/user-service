package io.fitcentive.user.modules

import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.sdk.gcp.pubsub.PubSubPublisher
import io.fitcentive.user.infrastructure.pubsub.SubscriptionManager
import io.fitcentive.user.modules.providers.SubscriptionManagerProvider
import io.fitcentive.user.services.SettingsService

import java.io.ByteArrayInputStream
import javax.inject.Singleton

class PubSubModule extends AbstractModule {

  @Provides
  @Singleton
  def providePubSubPublisher(settingsService: SettingsService): PubSubPublisher = {
    val credentials =
      ServiceAccountCredentials
        .fromStream(new ByteArrayInputStream(settingsService.serviceAccountStringCredentials.getBytes()))
        .createScoped()
    new PubSubPublisher(credentials, settingsService.gcpConfig.project)
  }

  override def configure(): Unit = {
    bind(classOf[SubscriptionManager]).toProvider(classOf[SubscriptionManagerProvider]).asEagerSingleton()
  }

}
