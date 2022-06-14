package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestUserAuthService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestUserAuthService])
trait UserAuthService {
  def createUserAccount(userId: UUID, email: String): Future[Either[DomainError, Unit]]
  def resetUserPassword(email: String, password: String): Future[Either[DomainError, Unit]]
  def updateUserProfile(
    email: String,
    authProvider: String,
    firstName: String,
    lastName: String
  ): Future[Either[DomainError, Unit]]
}
