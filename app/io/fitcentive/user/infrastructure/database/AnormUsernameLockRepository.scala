package io.fitcentive.user.infrastructure.database

import anorm.SqlParser
import io.fitcentive.user.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.user.infrastructure.database.AnormUsernameLockRepository.{
  SQL_ADD_USERNAME,
  SQL_GET_USERNAME,
  SQL_REMOVE_ALL,
  SQL_REMOVE_USERNAME
}
import io.fitcentive.user.repositories.UsernameLockRepository
import play.api.db.Database

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AnormUsernameLockRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UsernameLockRepository
  with DatabaseClient {

  override def removeAll: Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_REMOVE_ALL, Seq.empty)
    }

  override def removeUsername(username: String): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_REMOVE_USERNAME, Seq("username" -> username))
    }

  override def saveUsername(username: String): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_ADD_USERNAME, Seq("username" -> username))
    }

  override def getUsername(username: String): Future[Option[String]] =
    Future {
      getRecordOpt(SQL_GET_USERNAME, "username" -> username)(SqlParser.scalar[String])
    }
}

object AnormUsernameLockRepository {

  private val SQL_REMOVE_USERNAME: String =
    """
      |delete from username_lock
      |where username = {username}
      |""".stripMargin

  private val SQL_REMOVE_ALL: String =
    """
      |delete from username_lock
      |""".stripMargin

  private val SQL_GET_USERNAME: String =
    """
      |select *
      |from username_lock
      |where username = {username}
      |""".stripMargin

  private val SQL_ADD_USERNAME: String =
    """
      |insert into username_lock (username) 
      |values ({username}) ;
      |""".stripMargin
}
