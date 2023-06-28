package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class EventTrackingPayload(eventName: String, eventPlatform: String)

object EventTrackingPayload {
  implicit lazy val reads: Reads[EventTrackingPayload] = Json.reads[EventTrackingPayload]
  implicit lazy val writes: Writes[EventTrackingPayload] = Json.writes[EventTrackingPayload]
}
