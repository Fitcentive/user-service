package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.UserFollowRequest
import io.fitcentive.user.infrastructure.database.AnormUserFollowRequestRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserFollowRequestRepository])
trait UserFollowRequestRepository {
  def getUserFollowRequest(currentUserId: UUID, targetUserId: UUID): Future[Option[UserFollowRequest]]
  def deleteUserFollowRequest(currentUserId: UUID, targetUserId: UUID): Future[Unit]
  def requestToFollowUser(currentUserId: UUID, targetUserId: UUID): Future[UserFollowRequest]
}
