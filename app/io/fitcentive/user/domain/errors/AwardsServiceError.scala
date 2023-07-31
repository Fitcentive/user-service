package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class AwardsServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("edf1fae9-808b-4010-a85c-28ca37af6a01")
}
