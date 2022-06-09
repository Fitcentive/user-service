package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class VerifyEmailTokenPayload(email: String, token: String)

object VerifyEmailTokenPayload {
  implicit lazy val reads: Reads[VerifyEmailTokenPayload] = Json.reads[VerifyEmailTokenPayload]
  implicit lazy val writes: Writes[VerifyEmailTokenPayload] = Json.writes[VerifyEmailTokenPayload]
}
