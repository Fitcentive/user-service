package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class AuthUserDeletionError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("8636b1a3-2dad-4220-b6cb-0cd9dfb728e1")
}
