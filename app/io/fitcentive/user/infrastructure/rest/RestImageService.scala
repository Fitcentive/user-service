package io.fitcentive.user.infrastructure.rest

import akka.stream.scaladsl.{FileIO, Source}
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.config.ImageServiceConfig
import io.fitcentive.user.domain.errors.ImageUploadError
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.user.services.{ImageService, SettingsService}
import play.api.http.Status
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart

import java.io.File
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestImageService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends ImageService
  with ServiceSecretSupport {

  val imageServiceConfig: ImageServiceConfig = settingsService.imageServiceConfig
  val baseUrl: String = imageServiceConfig.serverUrl

  override def uploadImage(image: File, filePath: String): Future[Either[DomainError, String]] = {
    wsClient
      .url(s"$baseUrl/files/$filePath?token=${imageServiceConfig.token}")
      .addServiceSecret(settingsService)
      .put(Source(Seq(FilePart("file", filePath, Option.empty, FileIO.fromPath(image.toPath)))))
      .map { response =>
        response.status match {
          case Status.OK => Right(filePath)
          case status    => Left(ImageUploadError(s"Unexpected status from image-service: $status"))
        }
      }
  }
}
