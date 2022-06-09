package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class EntityConflictError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("c68d9c79-1df9-46e2-9b92-16e930c68ecd")
}
