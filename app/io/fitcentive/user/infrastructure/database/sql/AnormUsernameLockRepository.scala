package io.fitcentive.user.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.user.domain.lock.UsernameLock
import io.fitcentive.user.repositories.UsernameLockRepository
import play.api.db.Database

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AnormUsernameLockRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UsernameLockRepository
  with DatabaseClient {

  import AnormUsernameLockRepository._

  override def removeAll: Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_REMOVE_ALL, Seq.empty)
    }

  override def removeUsername(username: String): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_REMOVE_USERNAME, Seq("username" -> username))
    }

  override def saveUsername(username: String, userId: UUID): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_ADD_USERNAME, Seq("username" -> username, "userId" -> userId))
    }

  override def getUsername(username: String): Future[Option[UsernameLock]] =
    Future {
      getRecordOpt(SQL_GET_USERNAME, "username" -> username)(lockRowParser).map(_.toDomain)
    }
}

object AnormUsernameLockRepository {

  private val SQL_REMOVE_USERNAME: String =
    """
      |delete from username_lock
      |where username = {username} ;
      |""".stripMargin

  private val SQL_REMOVE_ALL: String =
    """
      |delete from username_lock ;
      |""".stripMargin

  private val SQL_GET_USERNAME: String =
    """
      |select *
      |from username_lock
      |where username = {username} ;
      |""".stripMargin

  private val SQL_ADD_USERNAME: String =
    """
      |insert into username_lock (username, user_id) 
      |values ({username},  {userId}) ;
      |""".stripMargin

  private case class LockRow(user_id: UUID, username: String) {
    def toDomain: UsernameLock = UsernameLock(userId = user_id, username = username)
  }

  private val lockRowParser: RowParser[LockRow] = Macro.namedParser[LockRow]
}
