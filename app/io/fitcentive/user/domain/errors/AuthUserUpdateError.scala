package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class AuthUserUpdateError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("4e941e6d-67fe-4086-a1f6-adb4498b2fba")
}
