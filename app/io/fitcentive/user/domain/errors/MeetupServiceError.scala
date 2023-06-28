package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class MeetupServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("79a4f437-8cd1-4bed-abef-5e1933839bbb")
}
