package io.fitcentive.user.api

import cats.data.EitherT
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.User
import io.fitcentive.user.domain.errors.EntityConflictError
import io.fitcentive.user.repositories.{EmailVerificationTokenRepository, UserRepository}
import io.fitcentive.user.services.{MessageBusService, TokenGenerationService, UserAuthService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginApi @Inject() (
  userRepository: UserRepository,
  userAuthService: UserAuthService,
  messageBusService: MessageBusService,
  tokenGenerationService: TokenGenerationService,
  emailVerificationTokenRepository: EmailVerificationTokenRepository
)(implicit ec: ExecutionContext) {

  def createNewUser(userCreate: User.Create): Future[Either[DomainError, User]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](
        userRepository
          .getUserByEmail(userCreate.email)
          .map(_.map(_ => Left(EntityConflictError("User with email already exists!"))).getOrElse(Right()))
      )
      user <- EitherT.right[DomainError](userRepository.createUser(userCreate))
      _ <- EitherT[Future, DomainError, Unit](
        userAuthService.createUserAccount(user.id, user.email, userCreate.ssoProvider)
      )
      _ <- EitherT.right[DomainError] {
        if (userCreate.ssoProvider.isEmpty) createAndPublishEmailVerificationTokenForUser(user) else Future.unit
      }
    } yield user).value

  private def createAndPublishEmailVerificationTokenForUser(user: User): Future[Unit] = {
    for {
      _ <- Future.unit
      emailVerificationToken = tokenGenerationService.generateEmailVerificationToken(user.email)
      _ <- emailVerificationTokenRepository.saveToken(emailVerificationToken)
      _ <- messageBusService.publishEmailVerificationTokenCreated(emailVerificationToken)
    } yield ()
  }

}
