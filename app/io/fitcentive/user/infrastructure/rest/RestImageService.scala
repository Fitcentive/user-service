package io.fitcentive.user.infrastructure.rest

import akka.stream.scaladsl.{FileIO, Source}
import com.google.auth.Credentials
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.config.ImageServiceConfig
import io.fitcentive.user.domain.errors.ImageUploadError
import io.fitcentive.user.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.user.services.{ImageService, SettingsService}
import play.api.http.Status
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData.FilePart
import com.google.cloud.storage.{Blob, Storage, StorageOptions}

import java.io.File
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class RestImageService @Inject() (wsClient: WSClient, credentials: Credentials, settingsService: SettingsService)(
  implicit ec: ExecutionContext
) extends ImageService
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

  override def deleteAllImagesForUser(userId: UUID): Future[Unit] =
    Future {
      StorageOptions.newBuilder
        .setProjectId(settingsService.gcpConfig.project)
        .setCredentials(credentials)
        .build
        .getService
        .pipe { storage =>
          storage
            .list(
              settingsService.userImageUploadBucket,
              Storage.BlobListOption.prefix(s"users/$userId"), // directoryPrefix is the sub directory.
              Storage.BlobListOption.currentDirectory()
            )
            .pipe { blobs =>
              blobs
                .iterateAll()
                .pipe { blobList =>
                  blobList.forEach(b => b.delete(Blob.BlobSourceOption.generationMatch))
                }
            }
        }
    }
}
