package io.fitcentive.user.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class RequestEmailVerificationTokenPayload(email: String)

object RequestEmailVerificationTokenPayload {
  implicit lazy val reads: Reads[RequestEmailVerificationTokenPayload] =
    Json.reads[RequestEmailVerificationTokenPayload]
  implicit lazy val writes: Writes[RequestEmailVerificationTokenPayload] =
    Json.writes[RequestEmailVerificationTokenPayload]
}
