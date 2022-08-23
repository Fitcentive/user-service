package io.fitcentive.user.domain

import play.api.libs.json.{JsString, Writes}

trait AuthProvider {
  def stringValue: String
}

object AuthProvider {
  def apply(status: String): AuthProvider =
    status match {
      case NativeAuth.stringValue   => NativeAuth
      case GoogleAuth.stringValue   => GoogleAuth
      case AppleAuth.stringValue    => AppleAuth
      case FacebookAuth.stringValue => FacebookAuth
      case _                        => throw new Exception("Unexpected auth provider")
    }

  implicit lazy val writes: Writes[AuthProvider] = {
    {
      case NativeAuth   => JsString(NativeAuth.stringValue)
      case GoogleAuth   => JsString(GoogleAuth.stringValue)
      case AppleAuth    => JsString(AppleAuth.stringValue)
      case FacebookAuth => JsString(FacebookAuth.stringValue)
    }
  }

  case object NativeAuth extends AuthProvider {
    val stringValue: String = "NativeAuth"
  }

  case object GoogleAuth extends AuthProvider {
    val stringValue: String = "GoogleAuth"
  }

  case object AppleAuth extends AuthProvider {
    val stringValue: String = "AppleAuth"
  }

  case object FacebookAuth extends AuthProvider {
    val stringValue: String = "FacebookAuth"
  }
}
