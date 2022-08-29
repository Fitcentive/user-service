package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.user.PublicUserProfile
import io.fitcentive.user.infrastructure.rest.RestSocialService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestSocialService])
trait SocialService {
  def upsertUser(user: PublicUserProfile): Future[Either[DomainError, Unit]]
  def deleteUserSocialMediaContent(userId: UUID): Future[Either[DomainError, Unit]]
  def deleteUserFromGraphDb(userId: UUID): Future[Either[DomainError, Unit]]
}
