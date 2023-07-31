package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestAwardsService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestAwardsService])
trait AwardsService {
  def deleteUserData(userId: UUID): Future[Either[DomainError, Unit]]
}
