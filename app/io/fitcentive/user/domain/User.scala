package io.fitcentive.user.domain

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

  case class Update(username: Option[String], accountStatus: Option[String], enabled: Option[Boolean])

  object Update {
    implicit lazy val reads: Reads[User.Update] = Json.reads[User.Update]
    implicit lazy val writes: Writes[User.Update] = Json.writes[User.Update]
  }

  case class Create(email: String, verificationToken: String)

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
