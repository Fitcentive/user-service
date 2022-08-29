package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class ChatServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("c0ba15e4-a5c3-483f-a304-0488a0396647")
}
