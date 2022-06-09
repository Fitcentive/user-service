package io.fitcentive.user.domain

import play.api.libs.json.{JsString, Json, Reads, Writes}

trait AccountStatus {
  def stringValue: String
}

object AccountStatus {
  def apply(status: String): AccountStatus =
    status match {
      case EmailVerificationRequired.stringValue  => EmailVerificationRequired
      case UsernameCreationRequired.stringValue   => UsernameCreationRequired
      case PasswordResetRequired.stringValue      => PasswordResetRequired
      case TermsAndConditionsRequired.stringValue => TermsAndConditionsRequired
      case LoginReady.stringValue                 => LoginReady
      case _                                      => throw new Exception("Unexpected account status")
    }

  implicit lazy val writes: Writes[AccountStatus] = {
    {
      case EmailVerificationRequired  => JsString(EmailVerificationRequired.stringValue)
      case UsernameCreationRequired   => JsString(UsernameCreationRequired.stringValue)
      case PasswordResetRequired      => JsString(PasswordResetRequired.stringValue)
      case TermsAndConditionsRequired => JsString(TermsAndConditionsRequired.stringValue)
      case LoginReady                 => JsString(LoginReady.stringValue)
    }
  }

  case object EmailVerificationRequired extends AccountStatus {
    val stringValue: String = "EmailVerificationRequired"
  }

  case object UsernameCreationRequired extends AccountStatus {
    val stringValue: String = "UsernameCreationRequired"
  }

  case object PasswordResetRequired extends AccountStatus {
    val stringValue: String = "PasswordResetRequired"
  }

  case object TermsAndConditionsRequired extends AccountStatus {
    val stringValue: String = "TermsAndConditionsRequired"
  }

  case object LoginReady extends AccountStatus {
    val stringValue: String = "LoginReady"
  }
}
