package io.fitcentive.user.domain

import play.api.libs.json.{JsString, Json, Reads, Writes}

trait EventPlatform {
  def stringValue: String
}

object EventPlatform {
  def apply(status: String): EventPlatform =
    status match {
      case Ios.stringValue     => Ios
      case Web.stringValue     => Web
      case Android.stringValue => Android
      case _                   => throw new Exception("Unexpected event platform status")
    }

  implicit lazy val writes: Writes[EventPlatform] = {
    {
      case Ios     => JsString(Ios.stringValue)
      case Web     => JsString(Web.stringValue)
      case Android => JsString(Android.stringValue)
    }
  }

  case object Ios extends EventPlatform {
    val stringValue: String = "iOS"
  }

  case object Android extends EventPlatform {
    val stringValue: String = "Android"
  }

  case object Web extends EventPlatform {
    val stringValue: String = "Web"
  }
}
