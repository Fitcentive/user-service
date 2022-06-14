package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class AuthUserCreationError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("364ff4c2-8f0d-4baa-9694-a40eae0450a0")
}
