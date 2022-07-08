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
  private def CYPHER_CREATE_USER_POST(post: Post.Create): DeferredQueryBuilder =
    c"""
      MATCH (u: User { userId: ${post.userId} })
      WITH u
      CREATE (u)-[:POSTED]->(post: Post { ${post.toNewInsertObject} } )
      RETURN post"""

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