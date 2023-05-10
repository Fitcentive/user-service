package io.fitcentive.user.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

import java.util.UUID

case class UserDisablePremiumEvent(userId: UUID) extends EventMessage

object UserDisablePremiumEvent extends PubSubOps {

  implicit val codec: Codec[UserDisablePremiumEvent] =
    deriveCodec[UserDisablePremiumEvent]

  implicit val converter: PubSubMessageConverter[UserDisablePremiumEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[UserDisablePremiumEvent]
}
