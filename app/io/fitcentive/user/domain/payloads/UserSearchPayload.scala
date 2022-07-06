package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class UserSearchPayload(query: String)

object UserSearchPayload {
  implicit lazy val reads: Reads[UserSearchPayload] = Json.reads[UserSearchPayload]
  implicit lazy val writes: Writes[UserSearchPayload] = Json.writes[UserSearchPayload]
}
