package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.infrastructure.database.AnormUsernameLockRepository

import scala.concurrent.Future

@ImplementedBy(classOf[AnormUsernameLockRepository])
trait UsernameLockRepository {
  def removeAll: Future[Unit]
  def removeUsername(username: String): Future[Unit]
  def saveUsername(username: String): Future[Unit]
  def getUsername(username: String): Future[Option[String]]
}
