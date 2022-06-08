package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.user.domain.errors.UserAuthAccountCreationError
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

  override def createUserAccount(
    userId: UUID,
    email: String,
    ssoProvider: Option[String],
    firstName: String,
    lastName: String
  ): Future[Either[UserAuthAccountCreationError, Unit]] = {
    wsClient
      .url(s"${userAuthServiceConfig.serverUrl}/api/auth/user")
      .withHttpHeaders("Content-Type" -> "application/json")
      .post(Json.toJson(CreateUserAuthAccountPayload(userId, email, firstName, lastName, ssoProvider)))
      .map { response =>
        response.status match {
          case Status.CREATED => Right(())
          case status         => Left(UserAuthAccountCreationError(s"Unexpected status from auth-service: ${status}"))
        }
      }
  }

}

object RestUserAuthService {

  case class CreateUserAuthAccountPayload(
    userId: UUID,
    email: String,
    firstName: String,
    lastName: String,
    ssoProvider: Option[String]
  )

  object CreateUserAuthAccountPayload {
    implicit val writes: Writes[CreateUserAuthAccountPayload] = Json.writes[CreateUserAuthAccountPayload]
  }

}
