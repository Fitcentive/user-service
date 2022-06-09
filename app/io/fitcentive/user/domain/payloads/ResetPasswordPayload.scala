package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

// todo - receive password as base64
case class ResetPasswordPayload(email: String, newPassword: String, emailVerificationToken: String)

object ResetPasswordPayload {
  implicit lazy val reads: Reads[ResetPasswordPayload] = Json.reads[ResetPasswordPayload]
  implicit lazy val writes: Writes[ResetPasswordPayload] = Json.writes[ResetPasswordPayload]
}
