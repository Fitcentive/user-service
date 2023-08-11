package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.infrastructure.pubsub.EventPublisherService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[EventPublisherService])
trait MessageBusService {
  def publishEmailVerificationTokenCreated(event: EmailVerificationToken): Future[Unit]
  def publishRequestDiaryToNotifyUsersRequiringNotificationToLogWeight(userIds: Seq[UUID]): Future[Unit]
  def publishRequestDiaryToNotifyUsersRequiringNotificationToLogDiaryEntry(userIds: Seq[UUID]): Future[Unit]
}
