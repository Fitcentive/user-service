package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.user.UserFriendRequest
import io.fitcentive.user.infrastructure.database.sql.AnormUserFriendRequestRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserFriendRequestRepository])
trait UserFriendRequestRepository {
  def getUserFriendRequest(currentUserId: UUID, targetUserId: UUID): Future[Option[UserFriendRequest]]
  def deleteUserFriendRequest(currentUserId: UUID, targetUserId: UUID): Future[Unit]
  def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Future[UserFriendRequest]
}
