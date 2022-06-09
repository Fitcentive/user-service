package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class EmailValidationError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("803b253b-8134-45cf-90ed-8aa213efb485")
}
