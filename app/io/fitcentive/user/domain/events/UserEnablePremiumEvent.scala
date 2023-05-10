package io.fitcentive.user.domain.events

import com.google.pubsub.v1.PubsubMessage
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.fitcentive.sdk.gcp.pubsub.PubSubMessageConverter
import io.fitcentive.sdk.utils.PubSubOps

import java.util.UUID

case class UserEnablePremiumEvent(userId: UUID) extends EventMessage

object UserEnablePremiumEvent extends PubSubOps {

  implicit val codec: Codec[UserEnablePremiumEvent] =
    deriveCodec[UserEnablePremiumEvent]

  implicit val converter: PubSubMessageConverter[UserEnablePremiumEvent] =
    (message: PubsubMessage) => message.decodeUnsafe[UserEnablePremiumEvent]
}
