package io.fitcentive.user.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.user.domain.user.UserTutorialStatus
import io.fitcentive.user.repositories.UserTutorialStatusRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserTutorialStatusRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserTutorialStatusRepository
  with DatabaseClient {

  import AnormUserTutorialStatusRepository._

  override def deleteUserTutorialStatus(userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_DELETE_USER_TUTORIAL_STATUS, Seq("userId" -> userId))
    }

  override def getUserTutorialStatus(userId: UUID): Future[Option[UserTutorialStatus]] =
    Future {
      getRecordOpt(SQL_GET_USER_TUTORIAL_STATUS, "userId" -> userId)(userTutorialStatusRowParser).map(_.toDomain)
    }

  override def markUserTutorialStatusAsComplete(userId: UUID): Future[UserTutorialStatus] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserTutorialStatusRow](
          SQL_MARK_USER_TUTORIAL_STATUS_AS_COMPLETE,
          Seq("userId" -> userId, "now" -> now)
        )(userTutorialStatusRowParser).toDomain
      }
    }

  override def markUserTutorialStatusAsIncomplete(userId: UUID): Future[UserTutorialStatus] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserTutorialStatusRow](
          SQL_MARK_USER_TUTORIAL_STATUS_AS_INCOMPLETE,
          Seq("userId" -> userId, "now" -> now)
        )(userTutorialStatusRowParser).toDomain
      }
    }
}

object AnormUserTutorialStatusRepository {

  private val SQL_DELETE_USER_TUTORIAL_STATUS: String =
    """
      |delete
      |from user_tutorial_status
      |where user_id = {userId}::uuid ;
      |""".stripMargin

  private val SQL_GET_USER_TUTORIAL_STATUS: String =
    """
      |select * 
      |from user_tutorial_status
      |where user_id = {userId}::uuid ;
      |""".stripMargin

  private val SQL_MARK_USER_TUTORIAL_STATUS_AS_COMPLETE: String =
    """
      |insert into user_tutorial_status (user_id, is_tutorial_complete, created_at, updated_at)
      |values ({userId}::uuid, true, {now}, {now})
      |on conflict (user_id)
      |do update set
      |  is_tutorial_complete = true,
      |  updated_at = {now} 
      |returning * ;
      |""".stripMargin

  private val SQL_MARK_USER_TUTORIAL_STATUS_AS_INCOMPLETE: String =
    """
      |insert into user_tutorial_status (user_id, is_tutorial_complete, created_at, updated_at)
      |values ({userId}::uuid, false, {now}, {now})
      |on conflict (user_id)
      |do update set
      |  is_tutorial_complete = false,
      |  updated_at = {now} 
      |returning * ;
      |""".stripMargin

  private case class UserTutorialStatusRow(
    user_id: UUID,
    is_tutorial_complete: Boolean,
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: UserTutorialStatus =
      UserTutorialStatus(
        userId = user_id,
        isTutorialComplete = is_tutorial_complete,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private val userTutorialStatusRowParser: RowParser[UserTutorialStatusRow] = Macro.namedParser[UserTutorialStatusRow]

}
