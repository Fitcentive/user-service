package io.fitcentive.user.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.sdk.utils.AnormOps
import io.fitcentive.user.domain.Gender
import io.fitcentive.user.domain.location.Coordinates
import io.fitcentive.user.domain.user.{PublicUserProfile, UserProfile}
import io.fitcentive.user.repositories.UserProfileRepository
import play.api.db.Database

import java.time.{Instant, LocalDate}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserProfileRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserProfileRepository
  with DatabaseClient {

  import AnormUserProfileRepository._

  override def getUserProfileByUserId(userId: UUID): Future[Option[UserProfile]] =
    Future {
      getRecordOpt(SQL_GET_USER_PROFILE_BY_ID, "userId" -> userId)(userProfileRowParser).map(_.toDomain)
    }

  override def createUserProfile(userId: UUID, userProfile: UserProfile.Update): Future[UserProfile] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserProfileRow](
          SQL_CREATE_AND_RETURN_USER_PROFILE(userProfile.locationCenter),
          Seq(
            "userId" -> userId,
            "firstName" -> userProfile.firstName,
            "lastName" -> userProfile.lastName,
            "photoUrl" -> userProfile.photoUrl,
            "dateOfBirth" -> userProfile.dateOfBirth,
            "now" -> now,
            "locationRadius" -> userProfile.locationRadius,
            "gender" -> userProfile.gender,
          )
        )(userProfileRowParser).toDomain
      }
    }

  override def updateUserProfilePost(userId: UUID, userProfile: UserProfile.Update): Future[UserProfile] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserProfileRow](
          SQL_UPDATE_AND_REPLACE_AND_RETURN_USER_PROFILE(userProfile.locationCenter),
          Seq(
            "userId" -> userId,
            "firstName" -> userProfile.firstName,
            "lastName" -> userProfile.lastName,
            "photoUrl" -> userProfile.photoUrl,
            "dateOfBirth" -> userProfile.dateOfBirth,
            "now" -> now,
            "locationRadius" -> userProfile.locationRadius,
            "gender" -> userProfile.gender,
          )
        )(userProfileRowParser).toDomain
      }
    }

  override def updateUserProfilePatch(userId: UUID, userProfile: UserProfile.Update): Future[UserProfile] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserProfileRow](
          SQL_UPDATE_AND_RETURN_USER_PROFILE(userProfile.locationCenter),
          Seq(
            "userId" -> userId,
            "firstName" -> userProfile.firstName,
            "lastName" -> userProfile.lastName,
            "photoUrl" -> userProfile.photoUrl,
            "dateOfBirth" -> userProfile.dateOfBirth,
            "now" -> now,
            "locationRadius" -> userProfile.locationRadius,
            "gender" -> userProfile.gender,
          )
        )(userProfileRowParser).toDomain
      }
    }

  override def getUserProfilesByIds(userIds: Seq[UUID]): Future[Seq[UserProfile]] =
    Future {
      getRecords(SQL_GET_USER_PROFILES_BY_IDS(userIds))(userProfileRowParser).map(_.toDomain)
    }

  override def getPublicUserProfilesByIds(userIds: Seq[UUID]): Future[Seq[PublicUserProfile]] =
    Future {
      getRecords(SQL_GET_PUBLIC_USER_PROFILES_BY_IDS(userIds))(publicUserProfileRowParser).map(_.toDomain)
    }

  override def getPublicUserProfileById(userId: UUID): Future[Option[PublicUserProfile]] =
    Future {
      getRecordOpt(SQL_GET_PUBLIC_USER_PROFILE_BY_ID, "userId" -> userId)(publicUserProfileRowParser).map(_.toDomain)
    }

  override def searchForUsers(searchQuery: String, limit: Int, offset: Int): Future[Seq[PublicUserProfile]] =
    Future {
      getRecords(SQL_SEARCH_BY_NAME_OR_USERNAME(searchQuery), "limit" -> limit, "offset" -> offset)(
        publicUserProfileRowParser
      ).map(_.toDomain)
    }
}

object AnormUserProfileRepository extends AnormOps {

  private def SQL_GET_USER_PROFILES_BY_IDS(userIds: Seq[UUID]): String = {
    val sql =
      """
        |select user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, gender
        |from user_profiles up
        |where up.user_id in (
        |""".stripMargin
    transformUuidsToSql(userIds, sql)
  }

  private val SQL_GET_PUBLIC_USER_PROFILE_BY_ID: String =
    """
        |select id, username, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, gender
        |from user_profiles up
        |left join users u
        |on up.user_id = u.id
        |where up.user_id = {userId}::uuid
        |""".stripMargin

  private def SQL_GET_PUBLIC_USER_PROFILES_BY_IDS(userIds: Seq[UUID]): String = {
    val sql =
      """
        |select id, username, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, gender
        |from user_profiles up
        |left join users u
        |on up.user_id = u.id
        |where up.user_id in (
        |""".stripMargin
    transformUuidsToSql(userIds, sql)
  }

  private val SQL_GET_USER_PROFILE_BY_ID: String =
    """
      |select user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, gender, created_at, updated_at
      |from user_profiles up
      |where up.user_id = {userId}::uuid ;
      |""".stripMargin

  private def SQL_CREATE_AND_RETURN_USER_PROFILE(coordinatesOpt: Option[Coordinates]): String = {
    coordinatesOpt match {
      case Some(Coordinates(lat, lng)) =>
        s"""
           |insert into user_profiles (user_id, first_name, last_name, photo_url, date_of_birth, location_center, location_radius, gender, created_at, updated_at)
           |values ({userId}::uuid, {firstName}, {lastName}, {photoUrl}, {dateOfBirth}, ST_GeomFromText('POINT($lat $lng)', $SRID), {locationRadius}, {gender}, {now}, {now})
           |returning user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, gender, created_at, updated_at;
           |""".stripMargin

      case None =>
        s"""
           |insert into user_profiles (user_id, first_name, last_name, photo_url, date_of_birth, location_radius, gender, created_at, updated_at)
           |values ({userId}::uuid, {firstName}, {lastName}, {photoUrl}, {dateOfBirth}, {locationRadius}, {gender}, {now}, {now})
           |returning user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, gender, created_at, updated_at;
           |""".stripMargin
    }
  }

  private def SQL_UPDATE_AND_RETURN_USER_PROFILE(coordinatesOpt: Option[Coordinates]): String = {
    coordinatesOpt match {
      case Some(Coordinates(lat, lng)) =>
        s"""
           |update user_profiles u
           |set 
           |first_name = case when {firstName} is null then u.first_name else {firstName} end, 
           |last_name = case when {lastName} is null then u.last_name else {lastName} end, 
           |photo_url = case when {photoUrl} is null then u.photo_url else {photoUrl} end, 
           |date_of_birth = case when {dateOfBirth}::date is null then u.date_of_birth else {dateOfBirth}::date end, 
           |location_radius = case when {locationRadius} is null then u.location_radius else {locationRadius} end, 
           |gender = case when {gender} is null then u.gender else {gender} end, 
           |location_center = ST_GeomFromText('POINT($lat $lng)', $SRID), 
           |updated_at = {now}
           |where u.user_id = {userId}::uuid 
           |returning user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, created_at, updated_at;
           |""".stripMargin

      case None =>
        s"""
           |update user_profiles u
           |set 
           |first_name = case when {firstName} is null then u.first_name else {firstName} end, 
           |last_name = case when {lastName} is null then u.last_name else {lastName} end, 
           |photo_url = case when {photoUrl} is null then u.photo_url else {photoUrl} end, 
           |date_of_birth = case when {dateOfBirth}::date is null then u.date_of_birth else {dateOfBirth}::date end, 
           |location_radius = case when {locationRadius} is null then u.location_radius else {locationRadius} end, 
           |gender = case when {gender} is null then u.gender else {gender} end,
           |updated_at = {now}
           |where u.user_id = {userId}::uuid 
           |returning user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, created_at, updated_at;
           |""".stripMargin
    }
  }

  private def SQL_UPDATE_AND_REPLACE_AND_RETURN_USER_PROFILE(coordinatesOpt: Option[Coordinates]): String =
    coordinatesOpt match {
      case Some(Coordinates(lat, lng)) =>
        s"""
           |update user_profiles u
           |set 
           |first_name = {firstName}, 
           |last_name = {lastName}, 
           |photo_url = {photoUrl}, 
           |date_of_birth = {dateOfBirth}::date, 
           |location_radius = {locationRadius}, 
           |location_center = ST_GeomFromText('POINT($lat $lng)', $SRID),
           |gender = {gender},
           |updated_at = {now}
           |where u.user_id = {userId}::uuid 
           |returning user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, created_at, updated_at;
           |""".stripMargin

      case None =>
        s"""
           |update user_profiles u
           |set 
           |first_name = {firstName}, 
           |last_name = {lastName}, 
           |photo_url = {photoUrl}, 
           |date_of_birth = {dateOfBirth}::date, 
           |location_radius = {locationRadius}, 
           |location_center = null,
           |gender = {gender},
           |updated_at = {now}
           |where u.user_id = {userId}::uuid 
           |returning user_id, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, created_at, updated_at;
           |""".stripMargin
    }

  private def SQL_SEARCH_BY_NAME_OR_USERNAME(searchQuery: String): String =
    s"""
       |select id, username, first_name, last_name, photo_url, date_of_birth, ST_ASTEXT(location_center) as location_center, location_radius, gender
       |from user_profiles up
       |left join users u
       |on up.user_id = u.id
       |where 
       |${compareAgainstUsernameAndNames(searchQuery)} 
       |limit {limit} 
       |offset {offset} ;
       |""".stripMargin

  private def compareAgainstUsernameAndNames(searchQuery: String): String = {
    val parts = searchQuery.split(" ")
    parts
      .map { part =>
        s" username ilike '%$part%' or first_name ilike '%$part%' or last_name ilike '%$part%' "
      }
      .mkString(" or ")
  }

  private case class UserProfileRow(
    user_id: UUID,
    first_name: Option[String],
    last_name: Option[String],
    photo_url: Option[String],
    date_of_birth: Option[LocalDate],
    location_center: Option[String],
    location_radius: Option[Int],
    gender: Option[String],
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: UserProfile =
      UserProfile(
        userId = user_id,
        firstName = first_name,
        lastName = last_name,
        photoUrl = photo_url,
        dateOfBirth = date_of_birth,
        locationCenter = location_center.map(Coordinates.fromString),
        locationRadius = location_radius,
        gender = gender.map(Gender.apply)
      )
  }

  private case class PublicUserProfileRow(
    id: UUID,
    username: Option[String],
    first_name: Option[String],
    last_name: Option[String],
    photo_url: Option[String],
    date_of_birth: Option[LocalDate],
    location_center: Option[String],
    location_radius: Option[Int],
    gender: Option[String],
  ) {
    def toDomain: PublicUserProfile =
      PublicUserProfile(
        userId = id,
        username = username,
        firstName = first_name,
        lastName = last_name,
        photoUrl = photo_url,
        dateOfBirth = date_of_birth,
        locationCenter = location_center.map(Coordinates.fromString),
        locationRadius = location_radius,
        gender = gender.map(Gender.apply)
      )
  }

  private val userProfileRowParser: RowParser[UserProfileRow] = Macro.namedParser[UserProfileRow]
  private val publicUserProfileRowParser: RowParser[PublicUserProfileRow] = Macro.namedParser[PublicUserProfileRow]

  // points in geographic wgs84 coordinates (epsg:4326)
  private val SRID: Int = 4326
}
