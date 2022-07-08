package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.social.Post
import io.fitcentive.user.infrastructure.database.graph.NeoTypesSocialMediaRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[NeoTypesSocialMediaRepository])
trait SocialMediaRepository {
  def createUserPost(post: Post.Create): Future[Post]
  def getPostsForUser(userId: UUID): Future[Seq[Post]]
  def getNewsfeedPostsForCurrentUser(userId: UUID): Future[Seq[Post]]
}
