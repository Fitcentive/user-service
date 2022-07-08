package io.fitcentive.user.domain.user

import play.api.libs.json.{Json, Reads, Writes}

import java.time.Instant
import java.util.UUID

case class UserAgreements(
  userId: UUID,
  termsAndConditionsAccepted: Boolean,
  subscribeToEmails: Boolean,
  createdAt: Instant,
  updatedAt: Instant
)

object UserAgreements {
  implicit lazy val writes: Writes[UserAgreements] = Json.writes[UserAgreements]
  implicit lazy val reads: Reads[UserAgreements] = Json.reads[UserAgreements]

  case class Update(termsAndConditionsAccepted: Option[Boolean], subscribeToEmails: Option[Boolean])

  object Update {
    implicit lazy val writes: Writes[UserAgreements.Update] = Json.writes[UserAgreements.Update]
    implicit lazy val reads: Reads[UserAgreements.Update] = Json.reads[UserAgreements.Update]
  }

  case class Create(termsAndConditionsAccepted: Boolean, subscribeToEmails: Boolean)

  object Create {
    implicit lazy val writes: Writes[UserAgreements.Create] = Json.writes[UserAgreements.Create]
    implicit lazy val reads: Reads[UserAgreements.Create] = Json.reads[UserAgreements.Create]
  }

}
