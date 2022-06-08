package io.fitcentive.user.infrastructure.verification

import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.services.TokenGenerationService

import javax.inject.Singleton
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.Random

@Singleton
class VerificationTokenGenerationService extends TokenGenerationService {

  import VerificationTokenGenerationService._

  override def generateEmailVerificationToken(emailId: String): EmailVerificationToken = {
    val token = {
      val size = 6
      (1 to size).map(_ => Random.shuffle(allCharacters.toList).head).mkString("")
    }
    EmailVerificationToken(emailId, token, System.currentTimeMillis() + EXPIRY_PERIOD.toMillis)
  }
}

object VerificationTokenGenerationService {
  val allCharacters: String = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789"
  val EXPIRY_PERIOD: FiniteDuration = 1.hour
}
