package io.fitcentive.user.domain.email

case class EmailVerificationToken(emailId: String, token: String, expiry: Long)
