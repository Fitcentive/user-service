package io.fitcentive.user.infrastructure.pubsub

import io.fitcentive.sdk.gcp.pubsub.{PubSubPublisher, PubSubSubscriber}
import io.fitcentive.sdk.logging.AppLogger
import io.fitcentive.user.domain.config.AppPubSubConfig
import io.fitcentive.user.domain.events.{
  ClearUsernameLockTableEvent,
  EventHandlers,
  PromptAllUsersDiaryEntryEvent,
  PromptAllUsersWeightEntryEvent,
  UserDisablePremiumEvent,
  UserEnablePremiumEvent
}
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
      _ <- subscribeToClearUsernameLockTableEvent
      _ <- subscribeToUserEnablePremiumEvents
      _ <- subscribeToUserDisablePremiumEvents
      _ <- subscribeToPromptUserWeightEntryEvent
      _ <- subscribeToPromptUserDiaryEntryEvent
      _ = logInfo("Subscriptions set up successfully!")
    } yield ()
  }

  private def subscribeToClearUsernameLockTableEvent: Future[Unit] =
    subscriber
      .subscribe[ClearUsernameLockTableEvent](
        environment,
        config.subscriptionsConfig.clearUsernameLockTableSubscription,
        config.topicsConfig.clearUsernameLockTableTopic
      )(_.payload.pipe(handleEvent))

  private def subscribeToUserDisablePremiumEvents: Future[Unit] =
    subscriber
      .subscribe[UserDisablePremiumEvent](
        environment,
        config.subscriptionsConfig.userDisablePremiumSubscription,
        config.topicsConfig.userDisablePremiumTopic
      )(_.payload.pipe(handleEvent))

  private def subscribeToUserEnablePremiumEvents: Future[Unit] =
    subscriber
      .subscribe[UserEnablePremiumEvent](
        environment,
        config.subscriptionsConfig.userEnablePremiumSubscription,
        config.topicsConfig.userEnablePremiumTopic
      )(_.payload.pipe(handleEvent))

  private def subscribeToPromptUserWeightEntryEvent: Future[Unit] =
    subscriber
      .subscribe[PromptAllUsersWeightEntryEvent](
        environment,
        config.subscriptionsConfig.promptAllUsersWeightEntrySubscription,
        config.topicsConfig.promptAllUsersWeightEntryTopic
      )(_.payload.pipe(handleEvent))

  private def subscribeToPromptUserDiaryEntryEvent: Future[Unit] =
    subscriber
      .subscribe[PromptAllUsersDiaryEntryEvent](
        environment,
        config.subscriptionsConfig.promptAllUsersDiaryEntrySubscription,
        config.topicsConfig.promptAllUsersDiaryEntryTopic
      )(_.payload.pipe(handleEvent))
}
