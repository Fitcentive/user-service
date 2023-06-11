package io.fitcentive.user.domain.user

import play.api.libs.json.{Json, Writes}

import java.time.Instant
import java.util.UUID

case class UserTutorialStatus(userId: UUID, isTutorialComplete: Boolean, createdAt: Instant, updatedAt: Instant)

object UserTutorialStatus {
  implicit lazy val writes: Writes[UserTutorialStatus] = Json.writes[UserTutorialStatus]
}
