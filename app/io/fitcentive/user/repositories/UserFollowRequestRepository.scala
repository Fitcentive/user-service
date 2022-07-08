package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.user.UserFollowRequest
import io.fitcentive.user.infrastructure.database.sql.AnormUserFollowRequestRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserFollowRequestRepository])
trait UserFollowRequestRepository {
  def getUserFollowRequest(currentUserId: UUID, targetUserId: UUID): Future[Option[UserFollowRequest]]
  def deleteUserFollowRequest(currentUserId: UUID, targetUserId: UUID): Future[Unit]
  def requestToFollowUser(currentUserId: UUID, targetUserId: UUID): Future[UserFollowRequest]
}
