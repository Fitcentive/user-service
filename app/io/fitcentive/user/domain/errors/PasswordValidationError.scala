package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class PasswordValidationError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("410fa25e-5d31-4323-a4fb-d5d24e148204")
}
