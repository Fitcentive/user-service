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

  case class Create(email: String, ssoProvider: Option[String] = None)

  object Create {
    implicit lazy val reads: Reads[User.Create] = Json.using[Json.WithDefaultValues].reads[User.Create]
    implicit lazy val writes: Writes[User.Create] = Json.writes[User.Create]
  }
}
