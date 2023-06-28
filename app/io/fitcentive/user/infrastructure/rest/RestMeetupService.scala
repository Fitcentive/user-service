package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.errors.MeetupServiceError
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.user.services.{MeetupService, SettingsService}
import play.api.http.Status
import play.api.libs.ws.WSClient

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestMeetupService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends MeetupService
  with ServiceSecretSupport {

  val meetupServiceConfig: ServerConfig = settingsService.meetupServiceConfig
  val baseUrl: String = meetupServiceConfig.serverUrl

  override def deleteUserMeetupData(userId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/meetup/user/$userId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.NO_CONTENT => Right(())
          case status            => Left(MeetupServiceError(s"Unexpected status from meetup-service: $status"))
        }
      }

}
