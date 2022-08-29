package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class DiscoverServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("609d526a-829a-4b84-8177-f2c042ed1f4d")
}
