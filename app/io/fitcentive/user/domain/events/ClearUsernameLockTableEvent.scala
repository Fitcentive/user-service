package io.fitcentive.user.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

case class ClearUsernameLockTableEvent(message: String) extends EventMessage

object ClearUsernameLockTableEvent extends PubSubOps {

  implicit val codec: Codec[ClearUsernameLockTableEvent] =
    deriveCodec[ClearUsernameLockTableEvent]

  implicit val converter: PubSubMessageConverter[ClearUsernameLockTableEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[ClearUsernameLockTableEvent]
}
