package io.fitcentive.user.infrastructure.settings

import com.google.auth.Credentials
import com.typesafe.config.Config
import io.fitcentive.sdk.config.{GcpConfig, JwtConfig, SecretConfig, ServerConfig}
import io.fitcentive.user.domain.config.{
  AppPubSubConfig,
  EnvironmentConfig,
  ImageServiceConfig,
  Neo4jConfig,
  SubscriptionsConfig,
  TopicsConfig
}
import io.fitcentive.user.services.SettingsService
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfigService @Inject() (config: Configuration, gcpCredentials: Credentials) extends SettingsService {

  override def neo4jConfig: Neo4jConfig = Neo4jConfig.fromConfig(config.get[Config]("neo4j"))

  override def envConfig: EnvironmentConfig = EnvironmentConfig.fromConfig(config.get[Config]("environment"))

  override def imageServiceConfig: ImageServiceConfig =
    ImageServiceConfig.fromConfig(config.get[Config]("services.image-service"))

  override def secretConfig: SecretConfig = SecretConfig.fromConfig(config.get[Config]("services"))

  override def keycloakServerUrl: String = config.get[String]("keycloak.server-url")

  override def jwtConfig: JwtConfig = JwtConfig.apply(config.get[Config]("jwt"))

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
