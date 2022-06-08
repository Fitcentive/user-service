package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.errors.UserAuthAccountCreationError
import io.fitcentive.user.infrastructure.rest.RestUserAuthService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestUserAuthService])
trait UserAuthService {
  def createUserAccount(
    userId: UUID,
    email: String,
    ssoProvider: Option[String],
    firstName: String,
    lastName: String
  ): Future[Either[UserAuthAccountCreationError, Unit]]
}
