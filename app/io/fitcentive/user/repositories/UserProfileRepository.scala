package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.UserProfile
import io.fitcentive.user.infrastructure.database.AnormUserProfileRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserProfileRepository])
trait UserProfileRepository {
  def getUserProfileByUserId(userId: UUID): Future[Option[UserProfile]]
  def createUserProfile(userId: UUID, user: UserProfile.Update): Future[UserProfile]
  def updateUserProfilePatch(userId: UUID, userProfile: UserProfile.Update): Future[UserProfile]
  def updateUserProfilePost(userId: UUID, userProfile: UserProfile.Update): Future[UserProfile]
  def getUserProfilesByIds(userIds: Seq[UUID]): Future[Seq[UserProfile]]
}
