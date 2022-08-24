package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.user.infrastructure.settings.AppHealthService

import scala.concurrent.Future

@ImplementedBy(classOf[AppHealthService])
trait HealthService {
  def isSqlDatabaseAvailable: Future[Boolean]
}
