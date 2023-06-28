package io.fitcentive.user.api

import io.fitcentive.user.domain.{EventPlatform, UserTrackingEvent}
import io.fitcentive.user.repositories.UserEventTrackingRepository

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventTrackingApi @Inject() (userEventTrackingRepository: UserEventTrackingRepository)(implicit
  ec: ExecutionContext
) {

  def addNewEvent(currentUserId: UUID, eventName: String, eventPlatform: String): Future[Unit] =
    userEventTrackingRepository.createNewEvent(
      currentUserId,
      UserTrackingEvent(eventName),
      EventPlatform(eventPlatform)
    )

}
