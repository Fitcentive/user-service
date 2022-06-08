package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.User
import io.fitcentive.user.infrastructure.database.PostgresUserRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[PostgresUserRepository])
trait UserRepository {
  // todo - what about conflicts?
  def createUser(user: User.Create, id: UUID = UUID.randomUUID()): Future[User]
}
