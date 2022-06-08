package io.fitcentive.user.services

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.infrastructure.verification.VerificationTokenGenerationService

import java.util.UUID

@ImplementedBy(classOf[VerificationTokenGenerationService])
trait TokenGenerationService {
  def generateEmailVerificationToken(emailId: String): EmailVerificationToken
}
