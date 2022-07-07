package io.fitcentive.user.domain.types

import neotypes.Driver

import scala.concurrent.Future

object CustomTypes {
  type GraphDb = Driver[Future]
}
