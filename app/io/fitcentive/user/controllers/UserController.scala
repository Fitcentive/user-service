package io.fitcentive.user.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.user.api.{LoginApi, UserApi}
import io.fitcentive.user.domain.payloads.{
  GetUserProfilesByIdsPayload,
  RequestEmailVerificationTokenPayload,
  ResetPasswordPayload,
  UserFollowRequestDecisionPayload,
  VerifyEmailTokenPayload
}
import io.fitcentive.user.domain.{User, UserAgreements, UserProfile}
import io.fitcentive.user.infrastructure.utils.ServerErrorHandler
import play.api.libs.json.Json
import play.api.mvc._

import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject() (
  loginApi: LoginApi,
  userApi: UserApi,
  userAuthAction: UserAuthAction,
  internalAuthAction: InternalAuthAction,
  cc: ControllerComponents
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  // -----------------------------
  // Unauthenticated routes
  // -----------------------------
  // todo - swagger docs, https://github.com/iheartradio/play-swagger
  def createUser: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[User.Create](request.body.asJson) { userCreate =>
        loginApi
          .createNewUser(userCreate)
          .map(handleEitherResult(_)(user => Created(Json.toJson(user))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def verifyEmailToken: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[VerifyEmailTokenPayload](request.body.asJson) { verifyEmailTokenPayload =>
        loginApi
          .verifyEmailToken(verifyEmailTokenPayload.email, verifyEmailTokenPayload.token)
          .map(handleEitherResult(_)(_ => NoContent))
          .recover(resultErrorAsyncHandler)
      }
    }

  def sendEmailVerificationToken: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[RequestEmailVerificationTokenPayload](request.body.asJson) { requestEmailVerificationTokenPayload =>
        loginApi
          .sendEmailVerificationToken(requestEmailVerificationTokenPayload.email)
          .map(handleEitherResult(_)(_ => Accepted))
          .recover(resultErrorAsyncHandler)
      }
    }

  def verifyEmail: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[RequestEmailVerificationTokenPayload](request.body.asJson) { requestEmailVerificationTokenPayload =>
        loginApi
          .verifyEmailForNewUserSignUp(requestEmailVerificationTokenPayload.email)
          .map(handleEitherResult(_)(_ => Accepted))
          .recover(resultErrorAsyncHandler)
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
          .recover(resultErrorAsyncHandler)
      }
    }

  def checkIfEmailExists(email: String): Action[AnyContent] =
    Action.async { implicit request =>
      userApi
        .checkIfUserExistsForEmail(email)
        .map {
          case true  => Ok
          case false => NotFound
        }
    }

  // -----------------------------
  // Internal Auth routes
  // -----------------------------
  def createOrUpdateUserProfileInternal(userId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[UserProfile.Update](request.body.asJson) { userProfileUpdate =>
        userApi
          .updateOrCreateUserProfile(userId, userProfileUpdate)
          .map(handleEitherResult(_)(userProfile => Ok(Json.toJson(userProfile))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def createSsoUser: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[User.CreateSsoUser](request.body.asJson) { userCreate =>
        loginApi
          .createNewSsoUser(userCreate)
          .map(handleEitherResult(_)(user => Created(Json.toJson(user))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def clearUsernameLockTable: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      userApi.clearUsernameLockTable
        .map(_ => NoContent)
        .recover(resultErrorAsyncHandler)
    }

  def getUserByEmail(email: String): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      userApi
        .getUserByEmail(email)
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
        .recover(resultErrorAsyncHandler)
    }

  // -----------------------------
  // User Auth routes
  // -----------------------------
  def updateUserPatch(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[User.Patch](userRequest.request.body.asJson) { userUpdate =>
          userApi
            .updateUserPatch(userId, userUpdate)
            .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def updateUserPost(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[User.Post](userRequest.request.body.asJson) { userUpdate =>
          userApi
            .updateUserPost(userId, userUpdate)
            .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def requestToFollowUser(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .requestToFollowUser(currentUserId, targetUserId)
          .map(handleEitherResult(_)(_ => Accepted))
          .recover(resultErrorAsyncHandler)
      }(userRequest, currentUserId)
    }

  def checkIfUserHasRequestedToFollowOtherUser(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .checkIfUserRequestedToFollowOtherUser(currentUserId, targetUserId)
          .map(handleEitherResult(_)(_ => Ok))
          .recover(resultErrorAsyncHandler)
      }(userRequest, currentUserId)
    }

  def applyUserFollowRequestDecision(currentUserId: UUID, requestingUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[UserFollowRequestDecisionPayload](userRequest.request.body.asJson) { decision =>
          userApi
            .applyUserFollowRequestDecision(currentUserId, requestingUserId, decision.isRequestApproved)
            .map(handleEitherResult(_)(_ => Ok))
            .recover(resultErrorAsyncHandler)
        }

      }(userRequest, currentUserId)
    }

  def getUser(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        userApi
          .getUser(userId)
          .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getUserUsername(userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      userApi
        .getUserUsername(userId)
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
        .recover(resultErrorAsyncHandler)
    }

  def createOrUpdateUserProfile(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[UserProfile.Update](userRequest.request.body.asJson) { userProfileUpdate =>
          userApi
            .updateOrCreateUserProfile(userId, userProfileUpdate)
            .map(handleEitherResult(_)(userProfile => Ok(Json.toJson(userProfile))))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def updateUserProfilePost(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[UserProfile.Update](userRequest.request.body.asJson) { userProfileUpdate =>
          userApi
            .updateUserProfilePost(userId, userProfileUpdate)
            .map(handleEitherResult(_)(userProfile => Ok(Json.toJson(userProfile))))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def getUserProfile(userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      userApi
        .getUserProfile(userId)
        .map(handleEitherResult(_)(userProfile => Ok(Json.toJson(userProfile))))
        .recover(resultErrorAsyncHandler)
    }

  def getUserAgreements(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        userApi
          .getUserAgreements(userId)
          .map(handleEitherResult(_)(userAgreements => Ok(Json.toJson(userAgreements))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def updateUserAgreements(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[UserAgreements.Update](userRequest.request.body.asJson) { userAgreementsUpdate =>
          userApi
            .updateUserAgreements(userId, userAgreementsUpdate)
            .map(handleEitherResult(_)(userAgreements => Ok(Json.toJson(userAgreements))))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def checkIfUsernameExists(username: String): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      userApi
        .checkIfUserExistsForUsername(username)
        .map {
          case true  => Ok
          case false => NotFound
        }
        .recover(resultErrorAsyncHandler)
    }

  def getUserProfilesByIds: Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[GetUserProfilesByIdsPayload](userRequest.request.body.asJson) { getUserByIdsPayload =>
        userApi
          .getUserProfilesByIds(getUserByIdsPayload.userIds)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

}
