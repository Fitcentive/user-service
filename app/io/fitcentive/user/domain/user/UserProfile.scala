package io.fitcentive.user.domain.user

import io.fitcentive.user.domain.location.Coordinates
import play.api.libs.json.{Json, Reads, Writes}

import java.time.LocalDate
import java.util.UUID

case class UserProfile(
  userId: UUID,
  firstName: Option[String],
  lastName: Option[String],
  photoUrl: Option[String],
  dateOfBirth: Option[LocalDate],
  locationCenter: Option[Coordinates],
  locationRadius: Option[Int]
) {
  def toUpdate: UserProfile.Update =
    UserProfile.Update(
      firstName = firstName,
      lastName = lastName,
      photoUrl = photoUrl,
      dateOfBirth = dateOfBirth,
      locationCenter = locationCenter,
      locationRadius = locationRadius
    )
}

object UserProfile {

  implicit lazy val writes: Writes[UserProfile] = Json.writes[UserProfile]

  case class Update(
    firstName: Option[String],
    lastName: Option[String],
    photoUrl: Option[String],
    dateOfBirth: Option[LocalDate],
    locationCenter: Option[Coordinates],
    locationRadius: Option[Int]
  )

  object Update {
    implicit lazy val reads: Reads[UserProfile.Update] = Json.reads[UserProfile.Update]
    implicit lazy val writes: Writes[UserProfile.Update] = Json.writes[UserProfile.Update]
  }

}
