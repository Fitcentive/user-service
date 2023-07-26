package io.fitcentive.user.api

import io.fitcentive.user.domain.{EventPlatform, UserTrackingEvent}
import io.fitcentive.user.repositories.UserEventTrackingRepository

import java.time.{LocalDate, ZoneOffset}
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventTrackingApi @Inject() (userEventTrackingRepository: UserEventTrackingRepository)(implicit
  ec: ExecutionContext
) {

  /**
    * Returns the total count of `ViewNewDiscoveredUser` events for the user for the given month
    */
  def getUserDiscoverInteractionCount(currentUserId: UUID, dateString: String, offsetInMinutes: Int): Future[Int] = {
    val date = LocalDate.parse(dateString)
    val startOfMonth = LocalDate.of(date.getYear, date.getMonth, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    val windowStart = startOfMonth.plus(-offsetInMinutes, ChronoUnit.MINUTES)
    val windowEnd = {
      if (List(4, 6, 9, 11).contains(date.getMonth.getValue)) windowStart.plus(30, ChronoUnit.DAYS)
      else if (List(1, 3, 5, 7, 8, 10, 12).contains(date.getMonth.getValue)) windowStart.plus(31, ChronoUnit.DAYS)
      else {
        if (date.getYear % 4 == 0) windowStart.plus(29, ChronoUnit.DAYS)
        else windowStart.plus(28, ChronoUnit.DAYS)
      }
    }
    userEventTrackingRepository
      .getEventsBetweenWindow(currentUserId, Seq(UserTrackingEvent.ViewNewDiscoveredUser), windowStart, windowEnd)
      .map(_.size)
  }

  def addNewEvent(currentUserId: UUID, eventName: String, eventPlatform: String): Future[Unit] =
    userEventTrackingRepository.createNewEvent(
      currentUserId,
      UserTrackingEvent(eventName),
      EventPlatform(eventPlatform)
    )

}
