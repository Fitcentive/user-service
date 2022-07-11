package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class SocialServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("9895d25a-073d-4419-aecb-eb18f71b86a6")
}
