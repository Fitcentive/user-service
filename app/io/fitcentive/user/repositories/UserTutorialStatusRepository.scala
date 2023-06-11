package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.user.UserTutorialStatus
import io.fitcentive.user.infrastructure.database.sql.AnormUserTutorialStatusRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserTutorialStatusRepository])
trait UserTutorialStatusRepository {
  def deleteUserTutorialStatus(userId: UUID): Future[Unit]
  def getUserTutorialStatus(userId: UUID): Future[Option[UserTutorialStatus]]
  def markUserTutorialStatusAsComplete(userId: UUID): Future[UserTutorialStatus]
  def markUserTutorialStatusAsIncomplete(userId: UUID): Future[UserTutorialStatus]
}
