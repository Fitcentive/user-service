package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class GetUsersByIdsPayload(userIds: Seq[UUID])

object GetUsersByIdsPayload {
  implicit lazy val reads: Reads[GetUsersByIdsPayload] = Json.reads[GetUsersByIdsPayload]
  implicit lazy val writes: Writes[GetUsersByIdsPayload] = Json.writes[GetUsersByIdsPayload]
}
