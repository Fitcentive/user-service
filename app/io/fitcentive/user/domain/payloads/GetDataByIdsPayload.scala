package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class GetDataByIdsPayload(userIds: Seq[UUID])

object GetDataByIdsPayload {
  implicit lazy val reads: Reads[GetDataByIdsPayload] = Json.reads[GetDataByIdsPayload]
  implicit lazy val writes: Writes[GetDataByIdsPayload] = Json.writes[GetDataByIdsPayload]
}
