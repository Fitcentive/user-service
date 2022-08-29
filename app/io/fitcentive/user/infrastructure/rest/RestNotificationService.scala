package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.errors.NotificationServiceError
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.user.services.{NotificationService, SettingsService}
import play.api.http.Status
import play.api.libs.ws.WSClient

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestNotificationService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: ExecutionContext
) extends NotificationService
  with ServiceSecretSupport {

  val notificationServiceConfig: ServerConfig = settingsService.notificationServiceConfig
  val baseUrl: String = notificationServiceConfig.serverUrl

  override def deleteUserNotificationData(userId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/notification/user/$userId/preferences")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.NO_CONTENT => Right(())
          case status            => Left(NotificationServiceError(s"Unexpected status from notification-service: $status"))
        }
      }
}
