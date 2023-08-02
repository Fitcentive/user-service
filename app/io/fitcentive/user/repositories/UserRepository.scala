package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.user.User
import io.fitcentive.user.infrastructure.database.sql.AnormUserRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserRepository])
trait UserRepository {
  def createStaticDeletedUser(userId: UUID, email: String): Future[User]
  def deleteUser(userId: UUID): Future[Unit]
  def createUser(user: User.Create, id: UUID = UUID.randomUUID()): Future[User]
  def createSsoUser(user: User.CreateSsoUser, id: UUID = UUID.randomUUID()): Future[User]
  def updateUserPatch(userId: UUID, user: User.Patch): Future[User]
  def updateUserPost(userId: UUID, user: User.Post): Future[User]
  def getUserByEmail(email: String): Future[Option[User]]
  def getUserByEmailAndRealm(email: String, realm: String): Future[Option[User]]
  def getUserByUsername(username: String): Future[Option[User]]
  def getUserById(id: UUID): Future[Option[User]]
  def getUsers: Future[Seq[User]]
  def getUsersByIds(userIds: Seq[UUID]): Future[Seq[User]]
  def getPremiumUsers: Future[Seq[User]]
  def enablePremium(userId: UUID): Future[Unit]
  def disablePremium(userId: UUID): Future[Unit]
}
