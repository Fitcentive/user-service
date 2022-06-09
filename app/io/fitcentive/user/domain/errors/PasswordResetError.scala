package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class PasswordResetError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("dfcea3b0-b20e-4ede-b910-f11b889d0696")
}
