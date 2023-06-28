package io.fitcentive.user.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.user.domain.{EventPlatform, UserTrackingEvent}
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.domain.event_tracking.UserEventTracking
import io.fitcentive.user.repositories.{EmailVerificationTokenRepository, UserEventTrackingRepository}
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserEventTrackingRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserEventTrackingRepository
  with DatabaseClient {

  import AnormUserEventTrackingRepository._

  override def createNewEvent(userId: UUID, event: UserTrackingEvent, platform: EventPlatform): Future[Unit] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithoutReturning(
          SQL_CREATE_NEW_EVENT,
          Seq(
            "eventId" -> UUID.randomUUID(),
            "userId" -> userId,
            "eventName" -> event.stringValue,
            "eventPlatform" -> platform.stringValue,
            "now" -> now
          )
        )
      }
    }
}

object AnormUserEventTrackingRepository {

  private val SQL_CREATE_NEW_EVENT: String =
    s"""
       |insert into user_event_tracking (event_id, user_id, event_name, event_platform, created_at)
       |values ({eventId}::uuid, {userId}::uuid, {eventName}, {eventPlatform}, {now})
       |""".stripMargin

  private case class UserEventTrackingRow(
    event_id: UUID,
    user_id: UUID,
    event_name: String,
    event_platform: String,
    created_at: Instant
  ) {
    def toDomain: UserEventTracking =
      UserEventTracking(
        eventId = event_id,
        userId = user_id,
        eventName = UserTrackingEvent(event_name),
        eventPlatform = EventPlatform(event_platform),
        createdAt = created_at
      )
  }

  private val userEventTrackingRowParser: RowParser[UserEventTrackingRow] = Macro.namedParser[UserEventTrackingRow]
}
