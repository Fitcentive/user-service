package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestNotificationService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestNotificationService])
trait NotificationService {
  def deleteUserNotificationData(userId: UUID): Future[Either[DomainError, Unit]]
}
