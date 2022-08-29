package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestImageService

import java.io.File
import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestImageService])
trait ImageService {
  def uploadImage(image: File, path: String): Future[Either[DomainError, String]]
  def deleteAllImagesForUser(userId: UUID): Future[Unit]
}
