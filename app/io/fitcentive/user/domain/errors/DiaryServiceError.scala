package io.fitcentive.user.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class DiaryServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("d47dd5af-6b51-4a30-be72-aba7e4340e0c")
}
