package io.fitcentive.user.infrastructure.database.graph

import io.fitcentive.user.domain.social.Post
import io.fitcentive.user.domain.types.CustomTypes.GraphDb
import io.fitcentive.user.infrastructure.contexts.Neo4jExecutionContext
import io.fitcentive.user.repositories.SocialMediaRepository
import neotypes.DeferredQueryBuilder
import neotypes.implicits.syntax.string._
import neotypes.generic.auto._
import neotypes.implicits.syntax.cypher._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class NeoTypesSocialMediaRepository @Inject() (val db: GraphDb)(implicit val ec: Neo4jExecutionContext)
  extends SocialMediaRepository {

  import NeoTypesSocialMediaRepository._

  override def getNewsfeedPostsForCurrentUser(userId: UUID): Future[Seq[Post]] =
    CYPHER_GET_USER_NEWSFEED_POSTS(userId)
      .readOnlyQuery[Post]
      .list(db)

  override def getPostsForUser(userId: UUID): Future[Seq[Post]] =
    CYPHER_GET_USER_POSTS(userId)
      .readOnlyQuery[Post]
      .list(db)

  override def createUserPost(post: Post.Create): Future[Post] =
    CYPHER_CREATE_USER_POST(post)
      .readOnlyQuery[Post]
      .single(db)
}

object NeoTypesSocialMediaRepository {
  private def CYPHER_CREATE_USER_POST(post: Post.Create): DeferredQueryBuilder = {
    val postInsert = post.toNewInsertObject
    c"""
      MATCH (u: User { userId: ${post.userId} })
      WITH u
      CREATE (post: Post { postId: ${postInsert.postId} } )
      SET
        post.userId = ${postInsert.userId},
        post.text = ${postInsert.text},
        post.photoUrl = ${postInsert.photoUrl},
        post.createdAt = ${postInsert.createdAt},
        post.updatedAt = ${postInsert.updatedAt}
      WITH u, post
      CREATE (u)-[:POSTED]->(post)
      WITH post.postId AS postId, post.userId AS userId, post.text AS text, 
           post.photoUrl AS photoUrl,  0 as numberOfLikes, 0 as numberOfComments, 
           post.createdAt AS createdAt, post.updatedAt AS updatedAt
      RETURN post.post, numberOfLikes, numberOfComments"""
  }

  private def CYPHER_GET_USER_POSTS(userId: UUID): DeferredQueryBuilder =
    c"""
      MATCH (currentUser: User { userId: $userId })-[:POSTED]->(post: Post)
      WITH post
      MATCH (u: User)-[rel:LIKED]->(post)
      WITH post, count(rel) AS numberOfLikes
      MATCH (post)-[:HAS_COMMENT]->(c: Comment)
      WITH post, numberOfLikes, count(c) AS numberOfComments
      ORDER BY post.updated_at DESC
      RETURN post, numberOfLikes, numberOfComments"""

  private def CYPHER_GET_USER_NEWSFEED_POSTS(currentUserId: UUID): DeferredQueryBuilder =
    c"""
      CALL {
        MATCH (current: User { userId: $currentUserId })-[:IS_FOLLOWING]->(friend: User)-[:POSTED]->(post: Post)
        WITH post
        MATCH (u: User)-[rel:LIKED]->(post)
        WITH post, count(rel) AS numberOfLikes
        MATCH (post)-[:HAS_COMMENT]->(c: Comment)
        WITH post, numberOfLikes, count(c) AS numberOfComments
        RETURN post, numberOfLikes, numberOfComments
          
        UNION
        
        MATCH (currentUser: User { userId: $currentUserId })-[:POSTED]->(post: Post)
        WITH post
        MATCH (u: User)-[rel:LIKED]->(post)
        WITH post, count(rel) AS numberOfLikes
        MATCH (post)-[:HAS_COMMENT]->(c: Comment)
        WITH post, numberOfLikes, count(c) AS numberOfComments
        RETURN post, numberOfLikes, numberOfComments        
      }
   
      RETURN post, numberOfLikes, numberOfComments
      ORDER BY post.updatedAt DESC"""

}
