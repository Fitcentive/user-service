package io.fitcentive.user.infrastructure.utils

import io.fitcentive.sdk.error.DomainError
import io.fitcentive.sdk.logging.AppLogger
import io.fitcentive.sdk.utils.DomainErrorHandler
import io.fitcentive.user.domain.errors.{EntityConflictError, EntityNotFoundError, UserCreationError}
import play.api.mvc.Result
import play.api.mvc.Results._

trait ServerErrorHandler extends DomainErrorHandler with AppLogger {

  override def resultErrorAsyncHandler: PartialFunction[Throwable, Result] = {
    case e: Exception =>
      logError(s"${e.getMessage}", e)
      InternalServerError(e.getMessage)
  }

  override def domainErrorHandler: PartialFunction[DomainError, Result] = {
    case UserCreationError(reason)   => BadRequest(reason)
    case EntityNotFoundError(reason) => NotFound(reason)
    case EntityConflictError(reason) => Conflict(reason)
  }

}
