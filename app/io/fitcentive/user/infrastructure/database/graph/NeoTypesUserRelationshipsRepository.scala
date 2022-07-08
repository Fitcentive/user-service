package io.fitcentive.user.infrastructure.database.graph

import neotypes.implicits.syntax.string._
import neotypes.generic.auto._
import neotypes.implicits.syntax.cypher._
import io.fitcentive.user.domain.PublicUserProfile
import io.fitcentive.user.domain.types.CustomTypes.GraphDb
import io.fitcentive.user.infrastructure.contexts.Neo4jExecutionContext
import io.fitcentive.user.repositories.UserRelationshipsRepository
import neotypes.DeferredQueryBuilder

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class NeoTypesUserRelationshipsRepository @Inject() (val db: GraphDb)(implicit val ec: Neo4jExecutionContext)
  extends UserRelationshipsRepository {

  import NeoTypesUserRelationshipsRepository._

  override def removeFollowerForUser(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_REMOVE_FOLLOWER_FOR_USER(requestingUserId, targetUserId)
      .readOnlyQuery[Unit]
      .single(db)

  override def makeUserUnFollowOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_UNFOLLOW_OTHER(requestingUserId, targetUserId)
      .readOnlyQuery[Unit]
      .single(db)

  override def makeUserFollowOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_FOLLOW_OTHER(requestingUserId, targetUserId)
      .readOnlyQuery[Unit]
      .single(db)

  override def upsertUser(user: PublicUserProfile): Future[PublicUserProfile] =
    CYPHER_UPSERT_USER_INFO(user)
      .readOnlyQuery[PublicUserProfile]
      .single(db)

  override def getUserFollowers(userId: UUID): Future[Seq[PublicUserProfile]] =
    CYPHER_GET_USER_FOLLOWERS(userId)
      .readOnlyQuery[PublicUserProfile]
      .list(db)

  override def getUserFollowing(userId: UUID): Future[Seq[PublicUserProfile]] =
    CYPHER_GET_USER_FOLLOWING(userId)
      .readOnlyQuery[PublicUserProfile]
      .list(db)

  override def getUserIfFollowingOtherUser(currentUser: UUID, otherUser: UUID): Future[Option[PublicUserProfile]] =
    CYPHER_GET_USER_IF_FOLLOWING_OTHER_USER(currentUser, otherUser)
      .readOnlyQuery[Option[PublicUserProfile]]
      .single(db)
}

object NeoTypesUserRelationshipsRepository {

  private def CYPHER_UPSERT_USER_INFO(user: PublicUserProfile): DeferredQueryBuilder =
    c"""
      MERGE (user: User { userID: ${user.userId} } )
      SET
        user.username = ${user.username},
        user.firstName = ${user.username},
        user.lastName = ${user.username},
        user.photoUrl = ${user.username},
        user.dateOfBirth = ${user.username}
      RETURN user"""

  private def CYPHER_MAKE_USER_FOLLOW_OTHER(requestingUserId: UUID, targetUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $requestingUserId } )
       MATCH (u2: User { userId: $targetUserId })
       MERGE (u1)-[r:IS_FOLLOWING]->(u2)
       RETURN u2"""

  private def CYPHER_GET_USER_FOLLOWERS(currentUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User)-[r:IS_FOLLOWING]->(u2: User { userId: $currentUserId})
       RETURN u1"""

  private def CYPHER_GET_USER_FOLLOWING(currentUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $currentUserId} )-[r:IS_FOLLOWING]->(u2: User)
       RETURN u2"""

  private def CYPHER_MAKE_USER_UNFOLLOW_OTHER(requestingUserId: UUID, targetUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $requestingUserId })-[r:IS_FOLLOWING]->(u2: User { userId: $targetUserId })
       DELETE r"""

  private def CYPHER_REMOVE_FOLLOWER_FOR_USER(currentUserId: UUID, followingUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $followingUserId })-[r:IS_FOLLOWING]->(u2: User { userId: $currentUserId })
       DELETE r"""

  private def CYPHER_GET_USER_IF_FOLLOWING_OTHER_USER(currentUserId: UUID, otherUserId: UUID): DeferredQueryBuilder =
    c"""
       OPTIONAL MATCH (u1: User { userId: $currentUserId })-[r:IS_FOLLOWING]->(u2: User { userId: $otherUserId })
       RETURN u1"""

}
