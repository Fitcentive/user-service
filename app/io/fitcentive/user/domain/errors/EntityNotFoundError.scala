package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class EntityNotFoundError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("c5a3d834-69dd-421b-b66e-0b0ab1a4298d")
}
