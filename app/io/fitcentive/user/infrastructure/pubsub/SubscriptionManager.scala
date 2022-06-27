package io.fitcentive.user.infrastructure.pubsub

import io.fitcentive.sdk.gcp.pubsub.{PubSubPublisher, PubSubSubscriber}
import io.fitcentive.sdk.logging.AppLogger
import io.fitcentive.user.domain.config.AppPubSubConfig
import io.fitcentive.user.domain.events.{ClearUsernameLockTableEvent, EventHandlers}
import io.fitcentive.user.infrastructure.contexts.PubSubExecutionContext

import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

class SubscriptionManager(
  publisher: PubSubPublisher,
  subscriber: PubSubSubscriber,
  config: AppPubSubConfig,
  environment: String
)(implicit ec: PubSubExecutionContext)
  extends AppLogger {

  self: EventHandlers =>

  initializeSubscriptions

  final def initializeSubscriptions: Future[Unit] = {
    for {
      _ <- Future.sequence(config.topicsConfig.topics.map(publisher.createTopic))
      _ <-
        subscriber
          .subscribe[ClearUsernameLockTableEvent](
            environment,
            config.subscriptionsConfig.clearUsernameLockTableSubscription,
            config.topicsConfig.clearUsernameLockTableTopic
          )(_.payload.pipe(handleEvent))
      _ = logInfo("Subscriptions set up successfully!")
    } yield ()
  }
}
