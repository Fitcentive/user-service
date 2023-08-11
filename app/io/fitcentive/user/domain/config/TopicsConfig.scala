package io.fitcentive.user.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(
  emailVerificationTokenCreatedTopic: String,
  clearUsernameLockTableTopic: String,
  userEnablePremiumTopic: String,
  userDisablePremiumTopic: String,
  promptAllUsersWeightEntryTopic: String,
  promptAllUsersDiaryEntryTopic: String,
  checkIfUsersNeedPromptToLogWeightTopic: String,
  checkIfUsersNeedPromptToLogDiaryEntriesTopic: String,
) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(
      emailVerificationTokenCreatedTopic,
      clearUsernameLockTableTopic,
      userEnablePremiumTopic,
      userDisablePremiumTopic,
      promptAllUsersWeightEntryTopic,
      promptAllUsersDiaryEntryTopic,
      checkIfUsersNeedPromptToLogWeightTopic,
      checkIfUsersNeedPromptToLogDiaryEntriesTopic,
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
      config.getString("prompt-all-users-diary-entry"),
      config.getString("check-if-users-need-prompt-to-log-weight"),
      config.getString("check-if-users-need-prompt-to-log-diary-entries"),
    )
}
