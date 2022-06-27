package io.fitcentive.user.infrastructure.handlers

import io.fitcentive.user.api.UserApi
import io.fitcentive.user.domain.events.{ClearUsernameLockTableEvent, EventHandlers, EventMessage}

import scala.concurrent.{ExecutionContext, Future}

trait MessageEventHandlers extends EventHandlers {

  def userApi: UserApi
  implicit def executionContext: ExecutionContext

  override def handleEvent(event: EventMessage): Future[Unit] =
    event match {
      case event: ClearUsernameLockTableEvent => userApi.clearUsernameLockTable
      case _                                  => Future.failed(new Exception("Unrecognized event"))
    }
}
