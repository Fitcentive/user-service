package io.fitcentive.user.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubSubscriptionConfig

case class SubscriptionsConfig(clearUsernameLockTableSubscription: String) extends PubSubSubscriptionConfig {
  val subscriptions: Seq[String] = Seq(clearUsernameLockTableSubscription)
}

object SubscriptionsConfig {
  def fromConfig(config: Config): SubscriptionsConfig =
    SubscriptionsConfig(config.getString("clear-username-lock-table"))
}
