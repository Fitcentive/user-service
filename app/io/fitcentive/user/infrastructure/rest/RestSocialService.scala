package io.fitcentive.user.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.errors.SocialServiceError
import io.fitcentive.user.domain.user.PublicUserProfile
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.user.services.{SettingsService, SocialService}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestSocialService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends SocialService
  with ServiceSecretSupport {

  val socialServiceConfig: ServerConfig = settingsService.socialServiceConfig
  val baseUrl: String = socialServiceConfig.serverUrl

  override def upsertUser(user: PublicUserProfile): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/social/user")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .post(Json.toJson(user))
      .map { response =>
        response.status match {
          case Status.OK => Right(())
          case status    => Left(SocialServiceError(s"Unexpected status from social-service: $status"))
        }
      }
}
