package io.fitcentive.user.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

case class PromptAllUsersDiaryEntryEvent(message: String) extends EventMessage

object PromptAllUsersDiaryEntryEvent extends PubSubOps {

  implicit val codec: Codec[PromptAllUsersDiaryEntryEvent] =
    deriveCodec[PromptAllUsersDiaryEntryEvent]

  implicit val converter: PubSubMessageConverter[PromptAllUsersDiaryEntryEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[PromptAllUsersDiaryEntryEvent]
}
