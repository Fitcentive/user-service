package io.fitcentive.user.infrastructure.handlers

import io.fitcentive.user.api.UserApi
import io.fitcentive.user.domain.events.{
  ClearUsernameLockTableEvent,
  EventHandlers,
  EventMessage,
  PromptAllUsersDiaryEntryEvent,
  PromptAllUsersWeightEntryEvent,
  UserDisablePremiumEvent,
  UserEnablePremiumEvent
}

import scala.concurrent.{ExecutionContext, Future}

trait MessageEventHandlers extends EventHandlers {

  def userApi: UserApi
  implicit def executionContext: ExecutionContext

  override def handleEvent(event: EventMessage): Future[Unit] =
    event match {
      case event: ClearUsernameLockTableEvent    => userApi.clearUsernameLockTable
      case event: UserDisablePremiumEvent        => userApi.disablePremiumForUser(event.targetUser)
      case event: UserEnablePremiumEvent         => userApi.enablePremiumForUser(event.targetUser)
      case event: PromptAllUsersWeightEntryEvent => userApi.notifyAllPremiumUsersToPromptForWeightEntry
      case event: PromptAllUsersDiaryEntryEvent  => userApi.notifyAllPremiumUsersToPromptForDiaryEntry
      case _                                     => Future.failed(new Exception("Unrecognized event"))
    }
}
