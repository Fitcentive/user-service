package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestDiaryService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestDiaryService])
trait DiaryService {
  def deleteUserDiaryData(userId: UUID): Future[Either[DomainError, Unit]]
}
