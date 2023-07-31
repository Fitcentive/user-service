package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.errors.AwardsServiceError
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.user.services.{AwardsService, SettingsService}
import play.api.http.Status
import play.api.libs.ws.WSClient

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestAwardsService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends AwardsService
  with ServiceSecretSupport {

  val awardsServiceConfig: ServerConfig = settingsService.awardsServiceConfig
  val baseUrl: String = awardsServiceConfig.serverUrl

  override def deleteUserData(userId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/awards/user/$userId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.NO_CONTENT => Right(())
          case status            => Left(AwardsServiceError(s"Unexpected status from awards-service: $status"))
        }
      }
}
