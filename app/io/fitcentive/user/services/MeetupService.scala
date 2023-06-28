package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestMeetupService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestMeetupService])
trait MeetupService {
  def deleteUserMeetupData(userId: UUID): Future[Either[DomainError, Unit]]
}
