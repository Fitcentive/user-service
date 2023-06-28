package io.fitcentive.user.domain.event_tracking

import io.fitcentive.user.domain.{EventPlatform, UserTrackingEvent}
import play.api.libs.json.{Json, Writes}

import java.time.Instant
import java.util.UUID

case class UserEventTracking(
  eventId: UUID,
  userId: UUID,
  eventName: UserTrackingEvent,
  eventPlatform: EventPlatform,
  createdAt: Instant
)

object UserEventTracking {
  implicit lazy val writes: Writes[UserEventTracking] = Json.writes[UserEventTracking]
}
