package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.User
import io.fitcentive.user.infrastructure.database.PostgresUserRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[PostgresUserRepository])
trait UserRepository {
  def createUser(user: User.Create, id: UUID = UUID.randomUUID()): Future[User]
  def updateUser(userId: UUID, user: User.Update): Future[User]
  def getUserByEmail(email: String): Future[Option[User]]
  def getUserById(id: UUID): Future[Option[User]]
}
