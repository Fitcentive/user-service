package io.fitcentive.user.infrastructure.database

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.sdk.utils.AnormOps
import io.fitcentive.user.domain.{AccountStatus, AuthProvider, User}
import io.fitcentive.user.repositories.UserRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserRepository
  with DatabaseClient {

  import AnormUserRepository._

  override def getUsersByIds(userIds: Seq[UUID]): Future[Seq[User]] =
    Future {
      getRecords(SQL_GET_USERS_BY_IDS, "userIds" -> userIds)(userRowParser).map(_.toDomain)
    }

  override def getUsers: Future[Seq[User]] =
    Future {
      getRecords(SQL_GET_ALL_USERS)(userRowParser).map(_.toDomain)
    }

  override def getUserById(id: UUID): Future[Option[User]] =
    Future {
      getRecordOpt(SQL_GET_USER_BY_ID, "id" -> id)(userRowParser).map(_.toDomain)
    }

  override def getUserByUsername(username: String): Future[Option[User]] =
    Future {
      getRecordOpt(SQL_GET_USER_BY_USERNAME, "username" -> username)(userRowParser).map(_.toDomain)
    }

  override def getUserByEmail(email: String): Future[Option[User]] =
    Future {
      getRecordOpt(SQL_GET_USER_BY_EMAIL, "email" -> email)(userRowParser).map(_.toDomain)
    }

  override def createUser(user: User.Create, id: UUID = UUID.randomUUID()): Future[User] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserRow](
          SQL_CREATE_AND_RETURN_NEW_USER,
          Seq(
            "id" -> id,
            "email" -> user.email,
            "username" -> None,
            "accountStatus" -> AccountStatus.ProfileInfoRequired.stringValue,
            "authProvider" -> AuthProvider.NativeAuth.stringValue,
            "enabled" -> true,
            "now" -> now,
          )
        )(userRowParser).toDomain
      }
    }

  override def createSsoUser(user: User.CreateSsoUser, id: UUID): Future[User] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserRow](
          SQL_CREATE_AND_RETURN_NEW_USER,
          Seq(
            "id" -> id,
            "email" -> user.email,
            "username" -> None,
            "accountStatus" -> AccountStatus.TermsAndConditionsRequired.stringValue,
            "authProvider" -> AuthProvider(user.ssoProvider).stringValue,
            "enabled" -> true,
            "now" -> now,
          )
        )(userRowParser).toDomain
      }
    }

  override def updateUserPost(userId: UUID, user: User.Post): Future[User] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserRow](
          SQL_UPDATE_AND_REPLACE_USER,
          Seq(
            "userId" -> userId,
            "username" -> user.username,
            "accountStatus" -> user.accountStatus,
            "enabled" -> user.enabled,
            "now" -> now
          )
        )(userRowParser).toDomain
      }
    }

  override def updateUserPatch(userId: UUID, user: User.Patch): Future[User] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserRow](
          generateSqlToUpdateAndReturnUser(user),
          Seq(
            "userId" -> userId,
            "username" -> user.username,
            "accountStatus" -> user.accountStatus,
            "enabled" -> user.enabled,
            "now" -> now
          )
        )(userRowParser).toDomain
      }
    }
}

object AnormUserRepository extends AnormOps {

  private case class UserRow(
    id: UUID,
    email: String,
    username: Option[String],
    account_status: String,
    auth_provider: String,
    enabled: Boolean,
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: User =
      User(
        id = id,
        email = email,
        username = username,
        accountStatus = AccountStatus(account_status),
        authProvider = AuthProvider(auth_provider),
        enabled = enabled,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private val userRowParser: RowParser[UserRow] = Macro.namedParser[UserRow]

  private val SQL_GET_USER_BY_ID: String =
    """
      |select *
      |from users u
      |where u.id = {id}::uuid
      |""".stripMargin

  private val SQL_GET_ALL_USERS: String =
    """
      |select *
      |from users u
      |""".stripMargin

  private val SQL_GET_USERS_BY_IDS: String =
    """
      |select *
      |from users u
      |where u.id in ({userIds}) ;
      |""".stripMargin

  private val SQL_GET_USER_BY_EMAIL: String =
    """
      |select * 
      |from users u
      |where u.email = {email} ;
      |""".stripMargin

  private val SQL_GET_USER_BY_USERNAME: String =
    """
      |select * 
      |from users u
      |where u.username = {username} ;
      |""".stripMargin

  private val SQL_CREATE_AND_RETURN_NEW_USER: String =
    """
      |insert into users (id, email, username, account_status, auth_provider, enabled, created_at, updated_at)
      |values ({id}::uuid, {email}, {username}, {accountStatus}, {authProvider}, {enabled}, {now}, {now})
      |returning * ;
      |""".stripMargin

  private val SQL_UPDATE_AND_REPLACE_USER: String =
    """
      |update users u
      |set username = {username}, account_status = {accountStatus}, enabled = {enabled}, updated_at = {now}
      |where u.id = {userId}::uuid
      |returning * ;
      |""".stripMargin

  private def generateSqlToUpdateAndReturnUser(user: User.Patch): String = {
    val setQuery = makeOptionalSqlUpdateParams(Macro.toParameters[User.Patch](user))
    s"""
       |update users u
       |set $setQuery, updated_at = {now}
       |where u.id = {userId}::uuid 
       |returning *;
       |""".stripMargin
  }
}
