package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.user.PublicUserProfile
import io.fitcentive.user.infrastructure.rest.RestSocialService

import scala.concurrent.Future

@ImplementedBy(classOf[RestSocialService])
trait SocialService {
  def upsertUser(user: PublicUserProfile): Future[Either[DomainError, Unit]]
}
