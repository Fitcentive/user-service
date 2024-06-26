package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.lock.UsernameLock
import io.fitcentive.user.infrastructure.database.sql.AnormUsernameLockRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUsernameLockRepository])
trait UsernameLockRepository {
  def removeAll: Future[Unit]
  def removeAllForUser(userId: UUID): Future[Unit]
  def removeUsername(username: String): Future[Unit]
  def saveUsername(username: String, userId: UUID): Future[Unit]
  def getUsername(username: String): Future[Option[UsernameLock]]
}
