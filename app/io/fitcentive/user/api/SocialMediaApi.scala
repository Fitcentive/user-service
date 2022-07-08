package io.fitcentive.user.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotAccessible}
import io.fitcentive.user.domain.social.Post
import io.fitcentive.user.domain.user.PublicUserProfile
import io.fitcentive.user.repositories.{SocialMediaRepository, UserRelationshipsRepository}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SocialMediaApi @Inject() (
  userRelationshipsRepository: UserRelationshipsRepository,
  socialMediaRepository: SocialMediaRepository
)(implicit ec: ExecutionContext) {

  def createPostForUser(post: Post.Create): Future[Post] =
    socialMediaRepository.createUserPost(post)

  def getPostsByUser(userId: UUID, requestingUserId: UUID): Future[Either[DomainError, Seq[Post]]] =
    (for {
      _ <- EitherT[Future, DomainError, PublicUserProfile](
        userRelationshipsRepository
          .getUserIfFollowingOtherUser(requestingUserId, userId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotAccessible("User not following other user!"))))
      )
      posts <- EitherT.right[DomainError](socialMediaRepository.getPostsForUser(userId))
    } yield posts).value

  /**
    * Returns posts belong to both current user as well posts of users being followed
    */
  def getNewsfeedPostsForUser(userId: UUID): Future[Seq[Post]] =
    socialMediaRepository.getNewsfeedPostsForCurrentUser(userId)

}
