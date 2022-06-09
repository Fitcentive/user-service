package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.UserProfile
import io.fitcentive.user.infrastructure.database.PostgresUserProfileRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[PostgresUserProfileRepository])
trait UserProfileRepository {
  def getUserProfileByUserId(userId: UUID): Future[Option[UserProfile]]
  def createUserProfile(userId: UUID, user: UserProfile.Update): Future[UserProfile]
  def updateUserProfile(userId: UUID, userProfile: UserProfile.Update): Future[UserProfile]
}
