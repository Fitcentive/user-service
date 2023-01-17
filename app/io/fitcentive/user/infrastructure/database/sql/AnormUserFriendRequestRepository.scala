package io.fitcentive.user.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.user.domain.user.UserFriendRequest
import io.fitcentive.user.repositories.UserFriendRequestRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserFriendRequestRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserFriendRequestRepository
  with DatabaseClient {

  import AnormUserFriendRequestRepository._

  override def deleteUserFriendRequest(currentUserId: UUID, targetUserId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_DELETE_USER_FRIEND_REQUEST,
        Seq("requestingUserId" -> currentUserId, "targetUserId" -> targetUserId)
      )
    }

  override def getUserFriendRequest(currentUserId: UUID, targetUserId: UUID): Future[Option[UserFriendRequest]] =
    Future {
      getRecordOpt(SQL_GET_USER_FRIEND_REQUEST, "requestingUserId" -> currentUserId, "targetUserId" -> targetUserId)(
        requestToFollowRowParser
      ).map(_.toDomain)
    }

  override def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Future[UserFriendRequest] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn(
          SQL_REQUEST_TO_FRIEND_USER,
          Seq("requestingUserId" -> currentUserId, "targetUserId" -> targetUserId, "now" -> now)
        )(requestToFollowRowParser).toDomain
      }
    }
}

object AnormUserFriendRequestRepository {

  private case class UserFriendRequestRow(requesting_user_id: UUID, target_user_id: UUID, created_at: Instant) {
    def toDomain: UserFriendRequest =
      UserFriendRequest(requestingUserId = requesting_user_id, targetUserId = target_user_id, createdAt = created_at)
  }

  private val SQL_GET_USER_FRIEND_REQUEST: String =
    """
      |select * from user_friend_requests
      |where requesting_user_id={requestingUserId}::uuid and target_user_id={targetUserId}::uuid ;
      |""".stripMargin

  private val SQL_DELETE_USER_FRIEND_REQUEST: String =
    """
      |delete from user_friend_requests
      |where requesting_user_id={requestingUserId}::uuid and target_user_id={targetUserId}::uuid ;
      |""".stripMargin

  private val SQL_REQUEST_TO_FRIEND_USER: String =
    """
      |insert into user_friend_requests (requesting_user_id, target_user_id, created_at)
      |values ({requestingUserId}::uuid, {targetUserId}::uuid, {now})
      |returning * ;
      |""".stripMargin

  private val requestToFollowRowParser: RowParser[UserFriendRequestRow] = Macro.namedParser[UserFriendRequestRow]
}
