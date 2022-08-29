package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class NotificationServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("71121ec6-6a01-4c24-96a0-a26ee79e5926")
}
