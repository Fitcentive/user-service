package io.fitcentive.user.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.user.api.{LoginApi, UserApi}
import io.fitcentive.user.domain.payloads._
import io.fitcentive.user.domain.user.{User, UserAgreements, UserProfile}
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
  def getPublicUserProfilesByIdsInternal: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[GetDataByIdsPayload](request.body.asJson) { getPublicUserProfilesByIdsPayload =>
        userApi
          .getPublicUserProfilesByIds(getPublicUserProfilesByIdsPayload.userIds)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getUserProfileInternal(userId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      userApi
        .getUserProfile(userId)
        .map(handleEitherResult(_)(userProfile => Ok(Json.toJson(userProfile))))
        .recover(resultErrorAsyncHandler)
    }

  def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit userRequest =>
      userApi
        .requestToFriendUser(currentUserId, targetUserId)
        .map(handleEitherResult(_)(_ => Accepted))
        .recover(resultErrorAsyncHandler)
    }

  def getUserFriendRequest(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit userRequest =>
      userApi
        .getUserFriendRequest(currentUserId, targetUserId)
        .map(handleEitherResult(_)(response => Ok(Json.toJson(response))))
        .recover(resultErrorAsyncHandler)
    }

  def deleteUserFriendRequest(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit userRequest =>
      userApi
        .deleteUserFriendRequest(currentUserId, targetUserId)
        .map(_ => Ok)
        .recover(resultErrorAsyncHandler)
    }

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

  def getUserByEmailAndRealm(email: String, realm: String): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      userApi
        .getUserByEmailAndRealm(email, realm)
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
        .recover(resultErrorAsyncHandler)
    }

  def getUserByEmail(email: String): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      userApi
        .getUserByEmail(email)
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
        .recover(resultErrorAsyncHandler)
    }

  def createStaticDeletedUser: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      loginApi.createStaticDeletedUser
        .map(handleEitherResult(_)(user => Ok(Json.toJson(user))))
        .recover(resultErrorAsyncHandler)
    }

  def getUserInternal(implicit userId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      userApi
        .getUser(userId)
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

  def searchForUser(limit: Option[Int] = None, offset: Option[Int] = None): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[UserSearchPayload](userRequest.request.body.asJson) { payload =>
        userApi
          .searchForUser(payload.query, limit, offset)
          .map(results => Ok(Json.toJson(results)))
          .recover(resultErrorAsyncHandler)
      }
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

  def checkIfUsernameExists(userId: UUID, username: String): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      userApi
        .checkIfUserExistsForUsername(username, userId)
        .map {
          case true  => Ok
          case false => NotFound
        }
        .recover(resultErrorAsyncHandler)
    }

  def getUserProfilesByIds: Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[GetDataByIdsPayload](userRequest.request.body.asJson) { getUserByIdsPayload =>
        userApi
          .getUserProfilesByIds(getUserByIdsPayload.userIds)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getPublicUserProfilesByIds: Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[GetDataByIdsPayload](userRequest.request.body.asJson) { getPublicUserProfilesByIdsPayload =>
        userApi
          .getPublicUserProfilesByIds(getPublicUserProfilesByIdsPayload.userIds)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def deleteUserAccount(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .deleteUserAccount(userId)
          .map(handleEitherResult(_)(_ => NoContent))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getUserTutorialStatus(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .getUserTutorialStatus(userId)
          .map(handleEitherResult(_)(status => Ok(Json.toJson(status))))
          .recover(resultErrorAsyncHandler)
      }
    }

  def markUserTutorialStatusAsComplete(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .markUserTutorialStatusAsComplete(userId)
          .map(status => Ok(Json.toJson(status)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def markUserTutorialStatusAsIncomplete(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .markUserTutorialStatusAsIncomplete(userId)
          .map(status => Ok(Json.toJson(status)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def deleteUserTutorialStatus(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userApi
          .deleteUserTutorialStatus(userId)
          .map(_ => NoContent)
          .recover(resultErrorAsyncHandler)
      }
    }

}
