package io.fitcentive.user.api

import io.fitcentive.user.domain.User
import io.fitcentive.user.repositories.{EmailVerificationTokenRepository, UserRepository}
import io.fitcentive.user.services.{MessageBusService, TokenGenerationService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserApi @Inject() (
  userRepository: UserRepository,
  messageBusService: MessageBusService,
  tokenGenerationService: TokenGenerationService,
  emailVerificationTokenRepository: EmailVerificationTokenRepository
)(implicit ec: ExecutionContext) {

  // todo - need to create keycloak yuser in auth service
  def createNewUser(userCreate: User.Create): Future[User] =
    for {
      user <- userRepository.createUser(userCreate)
      _ <- if (!userCreate.usesSso) createAndPublishEmailVerificationTokenForUser(user) else Future.unit
    } yield user

  private def createAndPublishEmailVerificationTokenForUser(user: User): Future[Unit] = {
    for {
      _ <- Future.unit
      emailVerificationToken = tokenGenerationService.generateEmailVerificationToken(user.email)
      _ <- emailVerificationTokenRepository.save(emailVerificationToken)
      _ <- messageBusService.publishEmailVerificationTokenCreated(emailVerificationToken)
    } yield ()
  }

}
