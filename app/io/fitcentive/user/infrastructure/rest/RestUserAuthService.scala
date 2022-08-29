package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.errors._
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
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
) extends UserAuthService
  with ServiceSecretSupport {

  import RestUserAuthService._

  val userAuthServiceConfig: ServerConfig = settingsService.authServiceConfig
  val baseUrl: String = userAuthServiceConfig.serverUrl

  override def updateUserProfile(
    email: String,
    authProvider: String,
    firstName: String,
    lastName: String
  ): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/auth/user/profile")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .post(Json.toJson(UpdateUserAuthProfilePayload(email, authProvider, firstName, lastName)))
      .map { response =>
        response.status match {
          case Status.OK => Right(())
          case status    => Left(AuthUserUpdateError(s"Unexpected status from auth-service: $status"))
        }
      }

  override def resetUserPassword(email: String, password: String): Future[Either[DomainError, Unit]] = {
    wsClient
      .url(s"$baseUrl/api/internal/auth/user/reset-password")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .post(Json.toJson(ResetPasswordPayload(email, password)))
      .map { response =>
        response.status match {
          case Status.OK => Right(())
          case status    => Left(PasswordResetError(s"Unexpected status from auth-service: $status"))
        }
      }
  }

  override def createUserAccount(userId: UUID, email: String): Future[Either[DomainError, Unit]] = {
    wsClient
      .url(s"$baseUrl/api/internal/auth/user")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .post(Json.toJson(CreateUserAuthAccountPayload(userId, email, "", "")))
      .map { response =>
        response.status match {
          case Status.CREATED => Right(())
          case status         => Left(AuthUserCreationError(s"Unexpected status from auth-service: $status"))
        }
      }
  }

  override def deleteUserByEmail(email: String, realm: String): Future[Either[DomainError, Unit]] = {
    wsClient
      .url(s"$baseUrl/api/internal/user/$email?realm=$realm")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.NO_CONTENT => Right(())
          case status            => Left(AuthUserDeletionError(s"Unexpected status from auth-service: $status"))
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

  case class UpdateUserAuthProfilePayload(email: String, authProvider: String, firstName: String, lastName: String)
  object UpdateUserAuthProfilePayload {
    implicit val writes: Writes[UpdateUserAuthProfilePayload] = Json.writes[UpdateUserAuthProfilePayload]
  }

}
