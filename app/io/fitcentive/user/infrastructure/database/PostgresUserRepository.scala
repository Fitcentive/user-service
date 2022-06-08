package io.fitcentive.user.infrastructure.database

import anorm.{Macro, RowParser}
import io.fitcentive.user.domain.{AccountStatus, User}
import io.fitcentive.user.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.user.repositories.UserRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class PostgresUserRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserRepository
  with DatabaseClient {

  import PostgresUserRepository._

  override def createUser(user: User.Create, id: UUID = UUID.randomUUID()): Future[User] =
    Future {
      val now = Instant.now()
      insertRecordWithExpectedReturn[UserRow](
        SQL_CREATE_AND_RETURN_NEW_USER,
        Seq(
          "id" -> id,
          "email" -> user.email,
          "username" -> user.username,
          "firstName" -> user.firstName,
          "lastName" -> user.lastName,
          "photoUrl" -> user.photoUrl,
          "accountStatus" -> user.accountStatus,
          "enabled" -> user.enabled,
          "now" -> now,
        )
      )(userRowParser).toDomain
    }
}

object PostgresUserRepository {

  private case class UserRow(
    id: UUID,
    email: String,
    username: String,
    first_name: String,
    last_name: String,
    photo_url: String,
    account_status: String,
    enabled: Boolean,
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: User =
      User(
        id = id,
        email = email,
        username = username,
        firstName = first_name,
        lastName = last_name,
        photoUrl = photo_url,
        accountStatus = AccountStatus(account_status),
        enabled = enabled,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private val userRowParser: RowParser[UserRow] = Macro.namedParser[UserRow]

  private val SQL_CREATE_AND_RETURN_NEW_USER: String =
    """
      |insert into users (id, email, username, first_name, last_name, photo_url, account_status, enabled, created_at, updated_at)
      |values ({id}::uuid, {email}, {username}, {firstName}, {lastName}, {photoUrl}, {accountStatus}, {enabled}, {now}, {now})
      |returning * ;
      |""".stripMargin
}
