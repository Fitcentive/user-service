package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.errors.{ChatServiceError, DiaryServiceError}
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.user.services.{DiaryService, SettingsService}
import play.api.http.Status
import play.api.libs.ws.WSClient

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestDiaryService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends DiaryService
  with ServiceSecretSupport {

  val diaryServiceConfig: ServerConfig = settingsService.diaryServiceConfig
  val baseUrl: String = diaryServiceConfig.serverUrl

  override def deleteUserDiaryData(userId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/diary/user/$userId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.NO_CONTENT => Right(())
          case status            => Left(DiaryServiceError(s"Unexpected status from diary-service: $status"))
        }
      }
}
