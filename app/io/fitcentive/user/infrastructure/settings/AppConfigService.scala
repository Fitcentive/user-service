package io.fitcentive.user.infrastructure.settings

import com.google.auth.Credentials
import com.typesafe.config.Config
import io.fitcentive.sdk.config.{GcpConfig, ServerConfig}
import io.fitcentive.user.domain.config.{AppPubSubConfig, SubscriptionsConfig, TopicsConfig}
import io.fitcentive.user.services.SettingsService
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfigService @Inject() (config: Configuration, gcpCredentials: Credentials) extends SettingsService {

  override def authServiceConfig: ServerConfig =
    ServerConfig.fromConfig(config.get[Config]("services.auth-service"))

  override def gcpConfig: GcpConfig =
    GcpConfig(credentials = gcpCredentials, project = config.get[String]("gcp.project"))

  override def pubSubConfig: AppPubSubConfig =
    AppPubSubConfig(
      topicsConfig = TopicsConfig.fromConfig(config.get[Config]("gcp.pubsub.topics")),
      subscriptionsConfig = SubscriptionsConfig.fromConfig(config.get[Config]("gcp.pubsub.subscriptions"))
    )
}
