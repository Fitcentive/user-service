package io.fitcentive.user.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}
import io.fitcentive.user.domain.{User, UserProfile}
import io.fitcentive.user.domain.errors.RequestParametersError
import io.fitcentive.user.repositories.{UserProfileRepository, UserRepository, UsernameLockRepository}
import io.fitcentive.user.services.UserAuthService

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserApi @Inject() (
  userRepository: UserRepository,
  userAuthService: UserAuthService,
  userProfileRepository: UserProfileRepository,
  usernameLockRepository: UsernameLockRepository
)(implicit ec: ExecutionContext) {

  // todo - create kubernetes cronJob to clear table out
  def clearUsernameLockTable: Future[Unit] =
    usernameLockRepository.removeAll

  def checkIfUserExistsForParameters(
    usernameOpt: Option[String],
    emailOpt: Option[String]
  ): Future[Either[DomainError, Boolean]] = {
    (usernameOpt, emailOpt) match {
      case (Some(username), None) => checkIfUserExistsForUsername(username).map(Right.apply)
      case (None, Some(email))    => checkIfUserExistsForEmail(email).map(Right.apply)
      case (Some(_), Some(_)) | (None, None) =>
        Future.successful(Left(RequestParametersError("At least one of `username` or `email` required")))
    }
  }

  def checkIfUserExistsForEmail(email: String): Future[Boolean] =
    userRepository.getUserByEmail(email).map(_.isDefined)

  /**
    * Checks if username is already in use or not
    * If username is available, then it is added to the username_lock table
    * username_lock table is cleared out periodically by k8s cron job
    */
  def checkIfUserExistsForUsername(username: String): Future[Boolean] =
    for {
      savedUsernameOpt <- userRepository.getUserByUsername(username)
      lockedUsernameOpt <- usernameLockRepository.getUsername(username)
      usernameExists = savedUsernameOpt.isDefined || lockedUsernameOpt.isDefined
      _ <- if (usernameExists) Future.unit else usernameLockRepository.saveUsername(username)
    } yield usernameExists

  def updateUser(userId: UUID, userUpdate: User.Update): Future[Either[DomainError, User]] =
    (for {
      userId <- EitherT[Future, DomainError, UUID](
        userRepository
          .getUserById(userId)
          .map(_.map(u => Right(u.id)).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      updatedUser <- EitherT.right[DomainError](userRepository.updateUser(userId, userUpdate))
    } yield updatedUser).value

  // todo - get user profile object along with user, separate APIs can exist
  def getUser(userId: UUID): Future[Either[DomainError, User]] =
    userRepository
      .getUserById(userId)
      .map(
        _.map(Right.apply)
          .getOrElse(Left(EntityNotFoundError("User not found!")))
      )

  def getUserByEmail(email: String): Future[Either[DomainError, User]] =
    userRepository
      .getUserByEmail(email)
      .map(
        _.map(Right.apply)
          .getOrElse(Left(EntityNotFoundError("User not found!")))
      )

  def getUsers: Future[Seq[User]] =
    userRepository.getUsers

  def getUserProfile(userId: UUID): Future[Either[DomainError, UserProfile]] =
    userProfileRepository
      .getUserProfileByUserId(userId)
      .map(
        _.map(Right.apply)
          .getOrElse(Left(EntityNotFoundError("User profile not found!")))
      )

  def updateOrCreateUserProfile(
    userId: UUID,
    userProfileUpdate: UserProfile.Update
  ): Future[Either[DomainError, UserProfile]] =
    (for {
      user <- EitherT[Future, DomainError, User](
        userRepository
          .getUserById(userId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      userProfileOpt <- EitherT.right[DomainError](userProfileRepository.getUserProfileByUserId(userId))
      updatedUserProfile <- EitherT.right[DomainError] {
        userProfileOpt match {
          case Some(_) => userProfileRepository.updateUserProfile(userId, userProfileUpdate)
          case None    => userProfileRepository.createUserProfile(userId, userProfileUpdate)
        }
      }
      _ <- EitherT[Future, DomainError, Unit](
        userAuthService
          .updateUserProfile(
            user.email,
            user.authProvider.stringValue,
            updatedUserProfile.firstName.optString,
            updatedUserProfile.lastName.optString
          )
      )
    } yield updatedUserProfile).value

  implicit class OptionalStringToEmpty(s: Option[String]) {
    def optString: String =
      s.fold("")(identity)
  }

}
