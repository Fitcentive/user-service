package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.infrastructure.database.PostgresEmailVerificationTokenRepository

import scala.concurrent.Future

@ImplementedBy(classOf[PostgresEmailVerificationTokenRepository])
trait EmailVerificationTokenRepository {
  def save(token: EmailVerificationToken): Future[Unit]
  def getEmailVerificationToken(email: String): Future[Option[EmailVerificationToken]]
}
