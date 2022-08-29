package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestDiscoverService

import scala.concurrent.Future
import java.util.UUID

@ImplementedBy(classOf[RestDiscoverService])
trait DiscoverService {
  def deleteUserDiscoverPreferences(userId: UUID): Future[Either[DomainError, Unit]]
}
