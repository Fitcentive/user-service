package io.fitcentive.user.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(
  emailVerificationTokenCreatedTopic: String,
  clearUsernameLockTableTopic: String,
  userFollowRequestedTopic: String
) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(emailVerificationTokenCreatedTopic, clearUsernameLockTableTopic, userFollowRequestedTopic)

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(
      config.getString("email-verification-token-created"),
      config.getString("clear-username-lock-table"),
      config.getString("user-follow-requested"),
    )
}
