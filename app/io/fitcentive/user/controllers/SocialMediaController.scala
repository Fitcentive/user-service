package io.fitcentive.user.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.user.api.SocialMediaApi
import io.fitcentive.user.domain.social.Post
import io.fitcentive.user.infrastructure.utils.ServerErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SocialMediaController @Inject() (
  socialMediaApi: SocialMediaApi,
  userAuthAction: UserAuthAction,
  cc: ControllerComponents
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  def createPostForUser(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[Post.Create](userRequest.request.body.asJson) { userPostCreate =>
          socialMediaApi
            .createPostForUser(userPostCreate)
            .map(post => Ok(Json.toJson(post)))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def getPostsForUser(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getPostsByUser(userId, userRequest.authorizedUser.userId)
        .map(handleEitherResult(_)(posts => Ok(Json.toJson(posts))))
        .recover(resultErrorAsyncHandler)
    }

  def getNewsfeedForUser(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getNewsfeedPostsForUser(userId)
        .map(posts => Ok(Json.toJson(posts)))
        .recover(resultErrorAsyncHandler)
    }

}
