package io.fitcentive.user.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}
import io.fitcentive.user.domain.{User, UserAgreements, UserProfile}
import io.fitcentive.user.domain.errors.RequestParametersError
import io.fitcentive.user.infrastructure.utils.ImageSupport
import io.fitcentive.user.repositories.{
  UserAgreementsRepository,
  UserProfileRepository,
  UserRepository,
  UsernameLockRepository
}
import io.fitcentive.user.services.{ImageService, UserAuthService}
import shapeless.ops.zipper.RightTo

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class UserApi @Inject() (
  userRepository: UserRepository,
  userAgreementsRepository: UserAgreementsRepository,
  userAuthService: UserAuthService,
  imageService: ImageService,
  userProfileRepository: UserProfileRepository,
  usernameLockRepository: UsernameLockRepository
)(implicit ec: ExecutionContext)
  extends ImageSupport {

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

  def updateUserPatch(userId: UUID, userUpdate: User.Patch): Future[Either[DomainError, User]] =
    (for {
      userId <- EitherT[Future, DomainError, UUID](
        userRepository
          .getUserById(userId)
          .map(_.map(u => Right(u.id)).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      updatedUser <- EitherT.right[DomainError](userRepository.updateUserPatch(userId, userUpdate))
    } yield updatedUser).value

  def updateUserPost(userId: UUID, userUpdate: User.Post): Future[Either[DomainError, User]] =
    (for {
      userId <- EitherT[Future, DomainError, UUID](
        userRepository
          .getUserById(userId)
          .map(_.map(u => Right(u.id)).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      updatedUser <- EitherT.right[DomainError](userRepository.updateUserPost(userId, userUpdate))
    } yield updatedUser).value

  def updateUserAgreements(
    userId: UUID,
    userAgreements: UserAgreements.Update
  ): Future[Either[DomainError, UserAgreements]] =
    (for {
      userId <- EitherT[Future, DomainError, UUID](
        userAgreementsRepository
          .getUserAgreementsByUserId(userId)
          .map(_.map(u => Right(u.userId)).getOrElse(Left(EntityNotFoundError("User agreement not found!"))))
      )
      updatedUserAgreements <-
        EitherT.right[DomainError](userAgreementsRepository.updateUserAgreements(userId, userAgreements))
    } yield updatedUserAgreements).value

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

  def getUsersByIds(userIds: Seq[UUID]): Future[Seq[User]] =
    userRepository.getUsersByIds(userIds)

  def getUserProfilesByIds(userIds: Seq[UUID]): Future[Seq[UserProfile]] =
    userProfileRepository.getUserProfilesByIds(userIds)

  def getUserProfile(userId: UUID): Future[Either[DomainError, UserProfile]] =
    userProfileRepository
      .getUserProfileByUserId(userId)
      .map(
        _.map(Right.apply)
          .getOrElse(Left(EntityNotFoundError("User profile not found!")))
      )

  def getUserAgreements(userId: UUID): Future[Either[DomainError, UserAgreements]] =
    userAgreementsRepository
      .getUserAgreementsByUserId(userId)
      .map(
        _.map(Right.apply)
          .getOrElse(Left(EntityNotFoundError("User agreements not found!")))
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
          case Some(_) => userProfileRepository.updateUserProfilePatch(userId, userProfileUpdate)
          case None    => userProfileRepository.createUserProfile(userId, userProfileUpdate)
        }
      }
      updatedUserProfileWithProfilePhoto <-
        EitherT[Future, DomainError, UserProfile](createUserImageIfRequired(updatedUserProfile))

      _ <- EitherT[Future, DomainError, Unit](
        userAuthService
          .updateUserProfile(
            user.email,
            user.authProvider.stringValue,
            updatedUserProfileWithProfilePhoto.firstName.optString,
            updatedUserProfileWithProfilePhoto.lastName.optString
          )
      )
    } yield updatedUserProfileWithProfilePhoto).value

  def updateUserProfilePost(
    userId: UUID,
    userProfileUpdate: UserProfile.Update
  ): Future[Either[DomainError, UserProfile]] =
    (for {
      user <- EitherT[Future, DomainError, User](
        userRepository
          .getUserById(userId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      _ <- EitherT[Future, DomainError, UserProfile](
        userProfileRepository
          .getUserProfileByUserId(userId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User profile not found!"))))
      )
      updatedUserProfile <-
        EitherT.right[DomainError](userProfileRepository.updateUserProfilePost(userId, userProfileUpdate))
      updatedUserProfileWithProfilePhoto <-
        EitherT[Future, DomainError, UserProfile](createUserImageIfRequired(updatedUserProfile))

      _ <- EitherT[Future, DomainError, Unit](
        userAuthService
          .updateUserProfile(
            user.email,
            user.authProvider.stringValue,
            updatedUserProfileWithProfilePhoto.firstName.optString,
            updatedUserProfileWithProfilePhoto.lastName.optString
          )
      )
    } yield updatedUserProfileWithProfilePhoto).value

  /**
    * Creates user image if
    *  1. PhotoURL is not present AND
    *  2. BOTH firstName AND lastName ARE present
    */
  private def createUserImageIfRequired(userProfile: UserProfile): Future[Either[DomainError, UserProfile]] =
    userProfile.photoUrl match {
      case None =>
        (userProfile.firstName, userProfile.lastName) match {
          case (Some(firstName), Some(lastName)) =>
            (for {
              userInitialsJpg <-
                EitherT.right[DomainError](Future.fromTry(Try(generateJpgWithUserInitials(firstName, lastName))))
              _ = userInitialsJpg.deleteOnExit()
              extension = userInitialsJpg.getName.split('.').last
              shaHash = calculateHash(userInitialsJpg)
              fileName = s"$shaHash.$extension"
              profilePhotoUploadPath = s"users/${userProfile.userId}/profile-photos/$fileName"
              _ <-
                EitherT[Future, DomainError, String](imageService.uploadImage(userInitialsJpg, profilePhotoUploadPath))
              userProfile <- EitherT.right[DomainError](
                userProfileRepository.updateUserProfilePatch(
                  userProfile.userId,
                  userProfile.toUpdate.copy(photoUrl = Some(profilePhotoUploadPath))
                )
              )
            } yield userProfile).value

          case _ => Future.successful(Right(userProfile))
        }
      case Some(_) => Future.successful(Right(userProfile))
    }

  implicit class OptionalStringToEmpty(s: Option[String]) {
    def optString: String =
      s.fold("")(identity)
  }

}
