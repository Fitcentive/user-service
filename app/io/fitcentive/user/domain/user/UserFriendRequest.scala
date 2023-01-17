package io.fitcentive.user.domain.user

import play.api.libs.json.{Json, Reads, Writes}

import java.time.Instant
import java.util.UUID

case class UserFriendRequest(requestingUserId: UUID, targetUserId: UUID, createdAt: Instant)

object UserFriendRequest {
  implicit lazy val writes: Writes[UserFriendRequest] = Json.writes[UserFriendRequest]
  implicit lazy val reads: Reads[UserFriendRequest] = Json.reads[UserFriendRequest]
}
