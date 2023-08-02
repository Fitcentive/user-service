package io.fitcentive.user.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

case class PromptAllUsersWeightEntryEvent(message: String) extends EventMessage

object PromptAllUsersWeightEntryEvent extends PubSubOps {

  implicit val codec: Codec[PromptAllUsersWeightEntryEvent] =
    deriveCodec[PromptAllUsersWeightEntryEvent]

  implicit val converter: PubSubMessageConverter[PromptAllUsersWeightEntryEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[PromptAllUsersWeightEntryEvent]
}
