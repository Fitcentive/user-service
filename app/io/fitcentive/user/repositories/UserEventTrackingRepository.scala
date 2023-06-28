package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.{EventPlatform, UserTrackingEvent}
import io.fitcentive.user.infrastructure.database.sql.AnormUserEventTrackingRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserEventTrackingRepository])
trait UserEventTrackingRepository {
  def createNewEvent(userId: UUID, event: UserTrackingEvent, platform: EventPlatform): Future[Unit]
}
