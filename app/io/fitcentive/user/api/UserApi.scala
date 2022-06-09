package io.fitcentive.user.api

import cats.data.EitherT
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.user.domain.{User, UserProfile}
import io.fitcentive.user.domain.errors.EntityNotFoundError
import io.fitcentive.user.repositories.{UserProfileRepository, UserRepository}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserApi @Inject() (userRepository: UserRepository, userProfileRepository: UserProfileRepository)(implicit
  ec: ExecutionContext
) {

  def updateUser(userId: UUID, userUpdate: User.Update): Future[Either[DomainError, User]] =
    (for {
      userId <- EitherT[Future, DomainError, UUID](
        userRepository
          .getUserById(userId)
          .map(_.map(u => Right(u.id)).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      updatedUser <- EitherT.right[DomainError](userRepository.updateUser(userId, userUpdate))
    } yield updatedUser).value

  def getUser(userId: UUID): Future[Either[DomainError, User]] =
    userRepository
      .getUserById(userId)
      .map(
        _.map(Right.apply)
          .getOrElse(Left(EntityNotFoundError("User not found!")))
      )

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
      _ <- EitherT[Future, DomainError, User](
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
    } yield updatedUserProfile).value

}
