package io.fitcentive.user.domain.user

import io.fitcentive.user.domain.{AccountStatus, AuthProvider}
import play.api.libs.json.{Json, Reads, Writes}

import java.time.Instant
import java.util.UUID

case class User(
  id: UUID,
  email: String,
  username: Option[String],
  accountStatus: AccountStatus,
  authProvider: AuthProvider,
  enabled: Boolean,
  createdAt: Instant,
  updatedAt: Instant
)

object User {

  implicit lazy val writes: Writes[User] = Json.writes[User]

  case class Post(username: Option[String], accountStatus: String, enabled: Boolean)

  object Post {
    implicit lazy val reads: Reads[User.Post] = Json.reads[User.Post]
    implicit lazy val writes: Writes[User.Post] = Json.writes[User.Post]
  }

  case class Patch(username: Option[String], accountStatus: Option[String], enabled: Option[Boolean])

  object Patch {
    implicit lazy val reads: Reads[User.Patch] = Json.reads[User.Patch]
    implicit lazy val writes: Writes[User.Patch] = Json.writes[User.Patch]
  }

  case class Create(
    email: String,
    verificationToken: String,
    termsAndConditionsAccepted: Boolean,
    subscribeToEmails: Boolean
  ) {
    def toUserAgreementsCreate: UserAgreements.Create =
      UserAgreements.Create(termsAndConditionsAccepted, subscribeToEmails)
  }

  case class CreateSsoUser(email: String, ssoProvider: String)

  object CreateSsoUser {
    implicit lazy val reads: Reads[CreateSsoUser] = Json.reads[CreateSsoUser]
    implicit lazy val writes: Writes[CreateSsoUser] = Json.writes[CreateSsoUser]
  }

  object Create {
    implicit lazy val reads: Reads[User.Create] = Json.reads[User.Create]
    implicit lazy val writes: Writes[User.Create] = Json.writes[User.Create]
  }
}
