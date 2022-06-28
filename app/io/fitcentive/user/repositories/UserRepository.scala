package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.User
import io.fitcentive.user.infrastructure.database.AnormUserRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserRepository])
trait UserRepository {
  def createUser(user: User.Create, id: UUID = UUID.randomUUID()): Future[User]
  def createSsoUser(user: User.CreateSsoUser, id: UUID = UUID.randomUUID()): Future[User]
  def updateUserPatch(userId: UUID, user: User.Patch): Future[User]
  def updateUserPost(userId: UUID, user: User.Post): Future[User]
  def getUserByEmail(email: String): Future[Option[User]]
  def getUserByUsername(username: String): Future[Option[User]]
  def getUserById(id: UUID): Future[Option[User]]
  def getUsers: Future[Seq[User]]
}
