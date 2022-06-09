package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class AuthProviderError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("13a3fa1f-e89d-475e-8dc9-302c287e89aa")
}
