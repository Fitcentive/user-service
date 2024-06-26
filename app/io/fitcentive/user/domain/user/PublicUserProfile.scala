package io.fitcentive.user.domain.user

import io.fitcentive.user.domain.Gender
import io.fitcentive.user.domain.location.Coordinates
import play.api.libs.json.{Json, Reads, Writes}

import java.time.LocalDate
import java.util.UUID

case class PublicUserProfile(
  userId: UUID,
  username: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  photoUrl: Option[String],
  dateOfBirth: Option[LocalDate],
  locationCenter: Option[Coordinates],
  locationRadius: Option[Int],
  gender: Option[Gender],
)

object PublicUserProfile {
  implicit lazy val writes: Writes[PublicUserProfile] = Json.writes[PublicUserProfile]
}
