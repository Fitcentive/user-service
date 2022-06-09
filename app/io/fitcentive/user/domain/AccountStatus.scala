package io.fitcentive.user.domain

import play.api.libs.json.{JsString, Json, Reads, Writes}

trait AccountStatus {
  def stringValue: String
}

object AccountStatus {
  def apply(status: String): AccountStatus =
    status match {
      case UsernameCreationRequired.stringValue => UsernameCreationRequired
      case ProfileInfoRequired.stringValue      => ProfileInfoRequired
      case LoginReady.stringValue               => LoginReady
      case _                                    => throw new Exception("Unexpected account status")
    }

  implicit lazy val writes: Writes[AccountStatus] = {
    {
      case UsernameCreationRequired => JsString(UsernameCreationRequired.stringValue)
      case ProfileInfoRequired      => JsString(ProfileInfoRequired.stringValue)
      case LoginReady               => JsString(LoginReady.stringValue)
    }
  }

  case object UsernameCreationRequired extends AccountStatus {
    val stringValue: String = "UsernameCreationRequired"
  }

  case object ProfileInfoRequired extends AccountStatus {
    val stringValue: String = "ProfileInfoRequired"
  }

  case object LoginReady extends AccountStatus {
    val stringValue: String = "LoginReady"
  }
}
