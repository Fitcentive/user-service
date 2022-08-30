package io.fitcentive.user.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityConflictError, EntityNotFoundError}
import io.fitcentive.user.domain.AuthProvider
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.domain.errors.{
  AuthProviderError,
  EmailValidationError,
  PasswordValidationError,
  TokenVerificationError
}
import io.fitcentive.user.domain.user.{User, UserAgreements, UserProfile}
import io.fitcentive.user.repositories.{
  EmailVerificationTokenRepository,
  UserAgreementsRepository,
  UserProfileRepository,
  UserRepository
}
import io.fitcentive.user.services.{MessageBusService, SettingsService, TokenGenerationService, UserAuthService}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginApi @Inject() (
  userRepository: UserRepository,
  userProfileRepository: UserProfileRepository,
  userAgreementsRepository: UserAgreementsRepository,
  userAuthService: UserAuthService,
  messageBusService: MessageBusService,
  tokenGenerationService: TokenGenerationService,
  emailVerificationTokenRepository: EmailVerificationTokenRepository,
  settingsService: SettingsService,
)(implicit ec: ExecutionContext) {

  import LoginApi._

  def createStaticDeletedUser: Future[Either[DomainError, User]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](
        userRepository
          .getUserByEmail(settingsService.staticDeletedUserEmail)
          .map(_.map(_ => Left(EntityConflictError("User with email already exists!"))).getOrElse(Right()))
      )
      user <- EitherT.right[DomainError](
        userRepository.createStaticDeletedUser(
          UUID.fromString(settingsService.staticDeletedUserId),
          settingsService.staticDeletedUserEmail
        )
      )
      userProfileUpdate = UserProfile.Update(
        firstName = Some("Deleted"),
        lastName = Some("User"),
        photoUrl = Some(s"deleted_user_avatar.png"), // This is already present in GCS
        dateOfBirth = None,
        locationCenter = None,
        locationRadius = None,
        gender = None
      )
      _ <- EitherT.right[DomainError](userProfileRepository.createUserProfile(user.id, userProfileUpdate))
    } yield user).value

  def createNewUser(userCreate: User.Create): Future[Either[DomainError, User]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](verifyEmailToken(userCreate.email, userCreate.verificationToken))
      _ <- EitherT[Future, DomainError, Unit](
        userRepository
          .getUserByEmail(userCreate.email)
          .map(_.map(_ => Left(EntityConflictError("User with email already exists!"))).getOrElse(Right()))
      )
      user <- EitherT.right[DomainError](userRepository.createUser(userCreate))
      _ <- EitherT.right[DomainError](
        userAgreementsRepository.createUserAgreements(user.id, userCreate.toUserAgreementsCreate)
      )
      _ <- EitherT[Future, DomainError, Unit](userAuthService.createUserAccount(user.id, user.email))
    } yield user).value

  /**
    * Assumes that the keycloak user already exists
    */
  def createNewSsoUser(userCreate: User.CreateSsoUser): Future[Either[DomainError, User]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](
        userRepository
          .getUserByEmail(userCreate.email)
          .map(_.map(_ => Left(EntityConflictError("User with email already exists!"))).getOrElse(Right()))
      )
      user <- EitherT.right[DomainError](userRepository.createSsoUser(userCreate))
      newUserAgreements = UserAgreements.Create(
        termsAndConditionsAccepted = false,
        privacyPolicyAccepted = false,
        subscribeToEmails = false
      )
      _ <- EitherT.right[DomainError](userAgreementsRepository.createUserAgreements(user.id, newUserAgreements))
    } yield user).value

  private def createAndPublishEmailVerificationTokenForUser(email: String): Future[Unit] = {
    for {
      _ <- Future.unit
      emailVerificationToken = tokenGenerationService.generateEmailVerificationToken(email)
      _ <- emailVerificationTokenRepository.removeTokensForEmail(email)
      _ <- emailVerificationTokenRepository.saveToken(emailVerificationToken)
      _ <- messageBusService.publishEmailVerificationTokenCreated(emailVerificationToken)
    } yield ()
  }

  def verifyEmailToken(email: String, token: String): Future[Either[DomainError, Unit]] = {
    emailVerificationTokenRepository
      .getEmailVerificationToken(email)
      .map(_.map {
        case verificationToken if isValidToken(verificationToken, token) => Right()
        case _                                                           => Left(TokenVerificationError("Invalid email verification token"))
      }.getOrElse(Left(EntityNotFoundError("No email token found"))))
  }

  def verifyEmailForNewUserSignUp(email: String): Future[Either[DomainError, Unit]] = {
    (for {
      _ <- EitherT[Future, DomainError, Unit](validateEmail(email))
      _ <- EitherT.right[DomainError](createAndPublishEmailVerificationTokenForUser(email))
    } yield ()).value
  }

  def sendEmailVerificationToken(email: String): Future[Either[DomainError, Unit]] = {
    (for {
      _ <- EitherT[Future, DomainError, Unit](validateEmail(email))
      _ <- EitherT(
        userRepository
          .getUserByEmail(email)
          .map(_.map {
            case user if user.authProvider == AuthProvider.NativeAuth => Right()
            case _                                                    => Left(AuthProviderError("Invalid authentication provider!"))
          }.getOrElse(Left(EntityNotFoundError("No user found!"))))
      )
      _ <- EitherT.right[DomainError](createAndPublishEmailVerificationTokenForUser(email))
    } yield ()).value
  }

  private def isValidToken(currentToken: EmailVerificationToken, incomingToken: String): Boolean =
    currentToken.token == incomingToken && currentToken.expiry > Instant.now.toEpochMilli

  private def validateEmail(email: String): Future[Either[DomainError, Unit]] =
    email match {
      case e if emailRegex.findFirstMatchIn(e).isDefined => Future.successful(Right())
      case _                                             => Future.successful(Left(EmailValidationError("Invalid email provided")))
    }

  private def verifyPasswordStrength(password: String): Future[Either[DomainError, Unit]] =
    password match {
      case e if passwordRegex.findFirstMatchIn(e).isDefined => Future.successful(Right())
      case _                                                => Future.successful(Left(PasswordValidationError("Password too weak!")))
    }

  def resetPassword(email: String, token: String, newPassword: String): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](verifyPasswordStrength(newPassword))
      _ <- EitherT[Future, DomainError, Unit](verifyEmailToken(email, token))
      _ <- EitherT[Future, DomainError, Unit](userAuthService.resetUserPassword(email, newPassword))
    } yield ()).value

}

object LoginApi {
  private val passwordRegex =
    """^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[!@#\$&*~^-_=+]).{8,}$""".r
  private val emailRegex =
    """^[a-zA-Z0-9\\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
}
