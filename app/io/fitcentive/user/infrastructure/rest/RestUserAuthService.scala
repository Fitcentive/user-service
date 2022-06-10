package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.errors.{PasswordResetError, UserCreationError}
import io.fitcentive.user.services.{SettingsService, UserAuthService}
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.WSClient

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestUserAuthService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: ExecutionContext
) extends UserAuthService {

  import RestUserAuthService._

  val userAuthServiceConfig: ServerConfig = settingsService.authServiceConfig

  // todo - make sure this URL isnt accessible from the outside world
  override def resetUserPassword(email: String, password: String): Future[Either[DomainError, Unit]] = {
    wsClient
      .url(s"${userAuthServiceConfig.serverUrl}/api/auth/user/reset-password")
      .withHttpHeaders("Content-Type" -> "application/json")
      .post(Json.toJson(ResetPasswordPayload(email, password)))
      .map { response =>
        response.status match {
          case Status.OK => Right(())
          case status    => Left(PasswordResetError(s"Unexpected status from auth-service: ${status}"))
        }
      }
  }

  override def createUserAccount(userId: UUID, email: String): Future[Either[DomainError, Unit]] = {
    wsClient
      .url(s"${userAuthServiceConfig.serverUrl}/api/auth/user")
      .withHttpHeaders("Content-Type" -> "application/json")
      .post(Json.toJson(CreateUserAuthAccountPayload(userId, email, "", "")))
      .map { response =>
        response.status match {
          case Status.CREATED => Right(())
          case status         => Left(UserCreationError(s"Unexpected status from auth-service: ${status}"))
        }
      }
  }

}

object RestUserAuthService {

  case class ResetPasswordPayload(email: String, password: String)

  object ResetPasswordPayload {
    implicit val writes: Writes[ResetPasswordPayload] = Json.writes[ResetPasswordPayload]
  }

  case class CreateUserAuthAccountPayload(userId: UUID, email: String, firstName: String, lastName: String)

  object CreateUserAuthAccountPayload {
    implicit val writes: Writes[CreateUserAuthAccountPayload] = Json.writes[CreateUserAuthAccountPayload]
  }

}
