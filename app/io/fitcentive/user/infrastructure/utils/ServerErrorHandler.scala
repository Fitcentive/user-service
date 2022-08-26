package io.fitcentive.user.infrastructure.utils

import io.fitcentive.sdk.error.{DomainError, EntityConflictError, EntityNotAccessible, EntityNotFoundError}
import io.fitcentive.sdk.logging.AppLogger
import io.fitcentive.sdk.utils.DomainErrorHandler
import io.fitcentive.user.domain.errors.{
  AuthProviderError,
  AuthUserCreationError,
  AuthUserUpdateError,
  EmailValidationError,
  ImageUploadError,
  PasswordResetError,
  PasswordValidationError,
  RequestParametersError,
  SocialServiceError,
  TokenVerificationError
}
import play.api.mvc.Result
import play.api.mvc.Results._

trait ServerErrorHandler extends DomainErrorHandler with AppLogger {

  override def resultErrorAsyncHandler: PartialFunction[Throwable, Result] = {
    case e: Exception =>
      logError(s"${e.getMessage}", e)
      InternalServerError(e.getMessage)
  }

  override def domainErrorHandler: PartialFunction[DomainError, Result] = {
    case AuthUserCreationError(reason)   => BadRequest(reason)
    case ImageUploadError(reason)        => InternalServerError(reason)
    case AuthProviderError(reason)       => BadRequest(reason)
    case AuthUserUpdateError(reason)     => BadRequest(reason)
    case TokenVerificationError(reason)  => Unauthorized(reason)
    case PasswordResetError(reason)      => BadRequest(reason)
    case EmailValidationError(reason)    => BadRequest(reason)
    case PasswordValidationError(reason) => BadRequest(reason)
    case RequestParametersError(reason)  => BadRequest(reason)
    case EntityNotFoundError(reason)     => NotFound(reason)
    case EntityConflictError(reason)     => Conflict(reason)
    case EntityNotAccessible(reason)     => Forbidden(reason)
    case SocialServiceError(reason)      => InternalServerError(reason)
    case _                               => InternalServerError("Unexpected error occurred ")
  }

}
