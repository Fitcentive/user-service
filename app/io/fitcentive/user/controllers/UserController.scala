package io.fitcentive.user.controllers

import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.user.api.{LoginApi, UserApi}
import io.fitcentive.user.domain.payloads.{
  RequestEmailVerificationTokenPayload,
  ResetPasswordPayload,
  VerifyEmailTokenPayload
}
import io.fitcentive.user.domain.{User, UserProfile}
import io.fitcentive.user.infrastructure.utils.ServerErrorHandler
import play.api.libs.json.Json
import play.api.mvc._

import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject() (loginApi: LoginApi, userApi: UserApi, cc: ControllerComponents)(implicit
  exec: ExecutionContext
) extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  def createUser: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[User.Create](request.body.asJson) { userCreate =>
        loginApi
          .createNewUser(userCreate)
          .map(handleEitherResult(_)(user => Created(Json.toJson(user))))
          .recover(resultErrorAsyncHandler)
      }
    }

  // todo - secure with token action
  def updateUser(userId: UUID): Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[User.Update](request.body.asJson) { userUpdate =>
        userApi
          .updateUser(userId, userUpdate)
          .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
      }
    }

  def getUser(userId: UUID): Action[AnyContent] =
    Action.async { implicit request =>
      userApi
        .getUser(userId)
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
    }

  def getUsers: Action[AnyContent] =
    Action.async { implicit request =>
      userApi.getUsers
        .map(users => Ok(Json.toJson(users)))
    }

  // todo - secure with token action
  def createOrUpdateUserProfile(userId: UUID): Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[UserProfile.Update](request.body.asJson) { userProfileUpdate =>
        userApi
          .updateOrCreateUserProfile(userId, userProfileUpdate)
          .map(handleEitherResult(_)(userProfile => Created(Json.toJson(userProfile))))
      }
    }

  def getUserProfile(userId: UUID): Action[AnyContent] =
    Action.async { implicit request =>
      userApi
        .getUserProfile(userId)
        .map(handleEitherResult(_)(userProfile => Ok(Json.toJson(userProfile))))
    }

  def checkIfUsernameExists(username: String): Action[AnyContent] =
    Action.async { implicit request =>
      userApi
        .checkIfUsernameExists(username)
        .map {
          case true  => Ok
          case false => NotFound
        }
    }

  // todo - need to secure this with client token or something, internal auth action?
  def clearUsernameLockTable: Action[AnyContent] =
    Action.async { implicit request =>
      userApi.clearUsernameLockTable
        .map(_ => NoContent)
    }

  def verifyEmailToken: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[VerifyEmailTokenPayload](request.body.asJson) { verifyEmailTokenPayload =>
        loginApi
          .verifyEmailToken(verifyEmailTokenPayload.email, verifyEmailTokenPayload.token)
          .map(handleEitherResult(_)(_ => NoContent))
      }
    }

  def sendEmailVerificationToken: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[RequestEmailVerificationTokenPayload](request.body.asJson) { requestEmailVerificationTokenPayload =>
        loginApi
          .sendEmailVerificationToken(requestEmailVerificationTokenPayload.email)
          .map(handleEitherResult(_)(_ => Accepted))
      }
    }

  def resetPassword: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[ResetPasswordPayload](request.body.asJson) { resetPasswordPayload =>
        loginApi
          .resetPassword(
            resetPasswordPayload.email,
            resetPasswordPayload.emailVerificationToken,
            resetPasswordPayload.newPassword
          )
          .map(handleEitherResult(_)(_ => Accepted))
      }
    }

}
