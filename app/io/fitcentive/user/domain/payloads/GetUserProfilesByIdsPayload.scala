package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class GetUserProfilesByIdsPayload(userIds: Seq[UUID])

object GetUserProfilesByIdsPayload {
  implicit lazy val reads: Reads[GetUserProfilesByIdsPayload] = Json.reads[GetUserProfilesByIdsPayload]
  implicit lazy val writes: Writes[GetUserProfilesByIdsPayload] = Json.writes[GetUserProfilesByIdsPayload]
}
