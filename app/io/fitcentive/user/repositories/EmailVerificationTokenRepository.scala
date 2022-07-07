package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.infrastructure.database.sql.AnormEmailVerificationTokenRepository

import scala.concurrent.Future

@ImplementedBy(classOf[AnormEmailVerificationTokenRepository])
trait EmailVerificationTokenRepository {
  def saveToken(token: EmailVerificationToken): Future[Unit]
  def getEmailVerificationToken(email: String): Future[Option[EmailVerificationToken]]
  def removeTokensForEmail(email: String): Future[Unit]
}
