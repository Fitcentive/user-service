package io.fitcentive.user.infrastructure.database

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.user.domain.UserFollowRequest
import io.fitcentive.user.repositories.UserFollowRequestRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserFollowRequestRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserFollowRequestRepository
  with DatabaseClient {

  import AnormUserFollowRequestRepository._

  override def deleteUserFollowRequest(currentUserId: UUID, targetUserId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_DELETE_USER_FOLLOW_REQUEST,
        Seq("requestingUserId" -> currentUserId, "targetUserId" -> targetUserId)
      )
    }

  override def getUserFollowRequest(currentUserId: UUID, targetUserId: UUID): Future[Option[UserFollowRequest]] =
    Future {
      getRecordOpt(SQL_GET_USER_FOLLOW_REQUEST, "requestingUserId" -> currentUserId, "targetUserId" -> targetUserId)(
        requestToFollowRowParser
      ).map(_.toDomain)
    }

  override def requestToFollowUser(currentUserId: UUID, targetUserId: UUID): Future[UserFollowRequest] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn(
          SQL_REQUEST_TO_FOLLOW_USER,
          Seq("requestingUserId" -> currentUserId, "targetUserId" -> targetUserId, "now" -> now)
        )(requestToFollowRowParser).toDomain
      }
    }
}

object AnormUserFollowRequestRepository {

  private case class UserFollowRequestRow(requesting_user_id: UUID, target_user_id: UUID, created_at: Instant) {
    def toDomain: UserFollowRequest =
      UserFollowRequest(requestingUserId = requesting_user_id, targetUserId = target_user_id, createdAt = created_at)
  }

  private val SQL_GET_USER_FOLLOW_REQUEST: String =
    """
      |select * from user_follow_requests
      |where requesting_user_id={requestingUserId}::uuid and target_user_id={targetUserId}::uuid ;
      |""".stripMargin

  private val SQL_DELETE_USER_FOLLOW_REQUEST: String =
    """
      |delete from user_follow_requests
      |where requesting_user_id={requestingUserId}::uuid and target_user_id={targetUserId}::uuid ;
      |""".stripMargin

  private val SQL_REQUEST_TO_FOLLOW_USER: String =
    """
      |insert into user_follow_requests (requesting_user_id, target_user_id, created_at)
      |values ({requestingUserId}::uuid, {targetUserId}::uuid, {now})
      |returning * ;
      |""".stripMargin

  private val requestToFollowRowParser: RowParser[UserFollowRequestRow] = Macro.namedParser[UserFollowRequestRow]
}
