package io.fitcentive.user.domain.lock

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class UsernameLock(userId: UUID, username: String)

object UsernameLock {
  implicit lazy val reads: Reads[UsernameLock] = Json.reads[UsernameLock]
  implicit lazy val writes: Writes[UsernameLock] = Json.writes[UsernameLock]
}
