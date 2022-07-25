package io.fitcentive.user.domain

import play.api.libs.json.{JsString, Writes}

trait Gender {
  def stringValue: String
}

object Gender {

  def apply(status: String): Gender =
    status match {
      case Male.stringValue   => Male
      case Female.stringValue => Female
      case Other.stringValue  => Other
      case _                  => throw new Exception("Unexpected gender")
    }

  implicit lazy val writes: Writes[Gender] = {
    {
      case Male   => JsString(Male.stringValue)
      case Female => JsString(Female.stringValue)
      case Other  => JsString(Other.stringValue)
    }
  }

  case object Male extends Gender {
    val stringValue: String = "Male"
  }

  case object Female extends Gender {
    val stringValue: String = "Female"
  }

  case object Other extends Gender {
    val stringValue: String = "Other"
  }
}
