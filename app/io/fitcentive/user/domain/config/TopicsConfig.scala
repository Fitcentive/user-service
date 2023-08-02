package io.fitcentive.user.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(
  emailVerificationTokenCreatedTopic: String,
  clearUsernameLockTableTopic: String,
  userEnablePremiumTopic: String,
  userDisablePremiumTopic: String,
  promptAllUsersWeightEntryTopic: String,
  promptUserToLogWeightTopic: String,
) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(
      emailVerificationTokenCreatedTopic,
      clearUsernameLockTableTopic,
      userEnablePremiumTopic,
      userDisablePremiumTopic,
      promptAllUsersWeightEntryTopic,
      promptUserToLogWeightTopic,
    )

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(
      config.getString("email-verification-token-created"),
      config.getString("clear-username-lock-table"),
      config.getString("user-enable-premium"),
      config.getString("user-disable-premium"),
      config.getString("prompt-all-users-weight-entry"),
      config.getString("prompt-user-to-log-weight"),
    )
}
