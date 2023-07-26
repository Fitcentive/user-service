package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class PublicGatewayServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("44419496-ff1e-4871-a821-5c099df05608")
}
