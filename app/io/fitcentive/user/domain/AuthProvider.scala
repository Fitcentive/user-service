package io.fitcentive.user.domain

import play.api.libs.json.{JsString, Writes}

trait AuthProvider {
  def stringValue: String
}

object AuthProvider {
  def apply(status: String): AuthProvider =
    status match {
      case NativeAuth.stringValue => NativeAuth
      case GoogleAuth.stringValue => GoogleAuth
      case _                      => throw new Exception("Unexpected auth provider")
    }

  implicit lazy val writes: Writes[AuthProvider] = {
    {
      case NativeAuth => JsString(NativeAuth.stringValue)
      case GoogleAuth => JsString(GoogleAuth.stringValue)
    }
  }

  case object NativeAuth extends AuthProvider {
    val stringValue: String = "NativeAuth"
  }

  case object GoogleAuth extends AuthProvider {
    val stringValue: String = "GoogleAuth"
  }

}
