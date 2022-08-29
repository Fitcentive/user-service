package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.infrastructure.rest.RestChatService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestChatService])
trait ChatService {
  def deleteUserChatData(userId: UUID): Future[Either[DomainError, Unit]]
}
