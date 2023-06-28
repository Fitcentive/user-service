package io.fitcentive.user.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.user.api.EventTrackingApi
import io.fitcentive.user.domain.payloads.EventTrackingPayload
import io.fitcentive.user.infrastructure.utils.ServerErrorHandler
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EventTrackingController @Inject() (
  eventTrackingApi: EventTrackingApi,
  userAuthAction: UserAuthAction,
  internalAuthAction: InternalAuthAction,
  cc: ControllerComponents
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  def addNewTrackingEvent: Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[EventTrackingPayload](userRequest.request.body.asJson) { payload =>
        eventTrackingApi
          .addNewEvent(userRequest.authorizedUser.userId, payload.eventName, payload.eventPlatform)
          .map(_ => NoContent)
          .recover(resultErrorAsyncHandler)
      }
    }

}
