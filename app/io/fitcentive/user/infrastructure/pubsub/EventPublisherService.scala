package io.fitcentive.user.infrastructure.pubsub

import io.fitcentive.registry.events.email.EmailVerificationTokenCreated
import io.fitcentive.sdk.gcp.pubsub.PubSubPublisher
import io.fitcentive.user.domain.config.TopicsConfig
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.infrastructure.contexts.PubSubExecutionContext
import io.fitcentive.user.services.{MessageBusService, SettingsService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class EventPublisherService @Inject() (publisher: PubSubPublisher, settingsService: SettingsService)(implicit
  ec: PubSubExecutionContext
) extends MessageBusService {

  private val publisherConfig: TopicsConfig = settingsService.pubSubConfig.topicsConfig

  override def publishEmailVerificationTokenCreated(event: EmailVerificationToken): Future[Unit] =
    event.toOut
      .pipe(publisher.publish(publisherConfig.emailVerificationTokenCreatedTopic, _))

  implicit class EmailVerificationTokenToOut(in: EmailVerificationToken) {
    def toOut: EmailVerificationTokenCreated =
      EmailVerificationTokenCreated(emailId = in.emailId, token = in.token, expiry = Some(in.expiry))
  }

}
