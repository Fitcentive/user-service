package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.social.{Post, PostComment}
import io.fitcentive.user.domain.user.PublicUserProfile
import io.fitcentive.user.infrastructure.database.graph.NeoTypesSocialMediaRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[NeoTypesSocialMediaRepository])
trait SocialMediaRepository {
  def createUserPost(post: Post.Create): Future[Post]
  def getPostsForUser(userId: UUID): Future[Seq[Post]]
  def getNewsfeedPostsForCurrentUser(userId: UUID): Future[Seq[Post]]
  def getUserIfLikedPost(userId: UUID, postId: UUID): Future[Option[PublicUserProfile]]
  def makeUserLikePost(userId: UUID, postId: UUID): Future[Unit]
  def makeUserUnlikePost(userId: UUID, postId: UUID): Future[Unit]
  def getUsersWhoLikedPost(postId: UUID): Future[Seq[PublicUserProfile]]
  def addCommentToPost(comment: PostComment.Create): Future[PostComment]
  def getCommentsForPost(postId: UUID): Future[Seq[PostComment]]
}
