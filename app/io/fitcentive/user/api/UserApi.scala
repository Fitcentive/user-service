package io.fitcentive.user.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}
import io.fitcentive.user.domain.user.{
  PublicUserProfile,
  User,
  UserAgreements,
  UserFriendRequest,
  UserProfile,
  UserTutorialStatus
}
import io.fitcentive.user.infrastructure.utils.ImageSupport
import io.fitcentive.user.repositories._
import io.fitcentive.user.services._

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class UserApi @Inject() (
  userRepository: UserRepository,
  userAgreementsRepository: UserAgreementsRepository,
  emailVerificationTokenRepository: EmailVerificationTokenRepository,
  userAuthService: UserAuthService,
  imageService: ImageService,
  userProfileRepository: UserProfileRepository,
  usernameLockRepository: UsernameLockRepository,
  userFriendRequestRepository: UserFriendRequestRepository,
  userTutorialStatusRepository: UserTutorialStatusRepository,
  socialService: SocialService,
  discoverService: DiscoverService,
  notificationService: NotificationService,
  chatService: ChatService,
  diaryService: DiaryService,
  meetupService: MeetupService,
  publicGatewayService: PublicGatewayService,
)(implicit ec: ExecutionContext)
  extends ImageSupport {

  val defaultLimit = 50
  val defaultOffset = 0

  def clearUsernameLockTable: Future[Unit] =
    usernameLockRepository.removeAll

  def enablePremiumForUser(userId: UUID): Future[Unit] =
    userRepository.enablePremium(userId)

  def disablePremiumForUser(userId: UUID): Future[Unit] =
    userRepository.disablePremium(userId)

  def checkIfUserExistsForEmail(email: String): Future[Boolean] =
    userRepository.getUserByEmail(email).map(_.isDefined)

  /**
    * Checks if username is already in use or not
    * Username is said to be in use iff
    *   1. User exists with username
    *   2. OR username exists in username_lock table AND is not reserved by current user in question
    * If username is available (not in use), then it is added to the username_lock table
    * username_lock table is cleared out periodically by k8s cron job
    */
  def checkIfUserExistsForUsername(username: String, userId: UUID): Future[Boolean] =
    for {
      savedUsernameOpt <- userRepository.getUserByUsername(username)
      lockedUsernameOpt <- usernameLockRepository.getUsername(username)
      usernameExists = savedUsernameOpt.isDefined || lockedUsernameOpt.exists(_.userId != userId)
      _ <- {
        if (usernameExists) Future.unit
        else {
          if (lockedUsernameOpt.exists(_.userId == userId)) Future.unit
          else usernameLockRepository.saveUsername(username, userId)
        }
      }
    } yield usernameExists

  def updateUserPatch(userId: UUID, userUpdate: User.Patch): Future[Either[DomainError, User]] =
    (for {
      userId <- EitherT[Future, DomainError, UUID](
        userRepository
          .getUserById(userId)
          .map(_.map(u => Right(u.id)).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      updatedUser <- EitherT.right[DomainError](userRepository.updateUserPatch(userId, userUpdate))
      publicUserProfile <-
        EitherT[Future, DomainError, PublicUserProfile](upsertPublicUserProfileIntoGraphDb(updatedUser.id))
    } yield updatedUser).value

  def updateUserPost(userId: UUID, userUpdate: User.Post): Future[Either[DomainError, User]] =
    (for {
      userId <- EitherT[Future, DomainError, UUID](
        userRepository
          .getUserById(userId)
          .map(_.map(u => Right(u.id)).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      updatedUser <- EitherT.right[DomainError](userRepository.updateUserPost(userId, userUpdate))
      publicUserProfile <-
        EitherT[Future, DomainError, PublicUserProfile](upsertPublicUserProfileIntoGraphDb(updatedUser.id))
    } yield updatedUser).value

  def getUserFriendRequest(requestingUserId: UUID, targetUserId: UUID): Future[Either[DomainError, UserFriendRequest]] =
    userFriendRequestRepository
      .getUserFriendRequest(requestingUserId, targetUserId)
      .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User follow request not found"))))

  /**
    * Deleting user account takes several steps
    * 1.  Delete email verification tokens
    * 2.  Delete from username_lock table
    * 3.  Delete from users table
    *     - cascade delete will take care of user_profiles, user_agreements, user_follow_requests and user_tutorial_status
    * 4.  Delete from Keycloak
    * 5.  Delete from social-service graph database (posts, comments, likes)
    * 6.  Delete from notification-service schema (devices, notification_data)
    * 7.  Delete from discover-service schema (postgres prefs, graph prefs)
    * 8.  Delete from diary-service schema
    * 9.  Delete from meetup-service schema
    * 10. Delete from public-gateway-service schema
    * 11  Delete from chat-service schema
    */
  def deleteUserAccount(userId: UUID): Future[Either[DomainError, Unit]] =
    (for {
      user <- EitherT[Future, DomainError, User](
        userRepository
          .getUserById(userId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      _ <- EitherT.right[DomainError](usernameLockRepository.removeAllForUser(userId))
      _ <- EitherT.right[DomainError](emailVerificationTokenRepository.removeTokensForEmail(user.email))
      _ <- EitherT.right[DomainError](imageService.deleteAllImagesForUser(userId))
      _ <- EitherT[Future, DomainError, Unit](socialService.deleteUserSocialMediaContent(user.id))
      _ <- EitherT[Future, DomainError, Unit](discoverService.deleteUserDiscoverPreferences(user.id))
      _ <- EitherT[Future, DomainError, Unit](notificationService.deleteUserNotificationData(user.id))
      _ <- EitherT[Future, DomainError, Unit](chatService.deleteUserChatData(user.id))
      _ <- EitherT[Future, DomainError, Unit](diaryService.deleteUserDiaryData(user.id))
      _ <- EitherT[Future, DomainError, Unit](meetupService.deleteUserMeetupData(user.id))
      _ <- EitherT[Future, DomainError, Unit](publicGatewayService.deleteUserData(user.id))

      // Finally, we delete the user login, user node and the user object itself
      _ <- EitherT.right[DomainError](socialService.deleteUserFromGraphDb(user.id))
      _ <- EitherT.right[DomainError](userRepository.deleteUser(userId))
      _ <-
        EitherT[Future, DomainError, Unit](userAuthService.deleteUserByEmail(user.email, user.authProvider.stringValue))
    } yield ()).value

  def deleteUserFriendRequest(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    userFriendRequestRepository.deleteUserFriendRequest(requestingUserId, targetUserId)

  def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, User](
        userRepository
          .getUserById(currentUserId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      _ <- EitherT[Future, DomainError, User](
        userRepository
          .getUserById(targetUserId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User not found!"))))
      )
      _ <- EitherT.right[DomainError](userFriendRequestRepository.requestToFriendUser(currentUserId, targetUserId))
    } yield ()).value

  def searchForUser(
    searchQuery: String,
    limit: Option[Int] = None,
    offset: Option[Int] = None
  ): Future[Seq[PublicUserProfile]] =
    userProfileRepository
      .searchForUsers(searchQuery, limit.fold(defaultLimit)(identity), offset.fold(defaultOffset)(identity))

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

  def getUser(userId: UUID): Future[Either[DomainError, User]] =
    userRepository
      .getUserById(userId)
      .map(
        _.map(Right.apply)
          .getOrElse(Left(EntityNotFoundError("User not found!")))
      )

  def getUserUsername(userId: UUID): Future[Either[DomainError, String]] =
    userRepository
      .getUserById(userId)
      .map(
        _.map(user => user.username.map(Right.apply).getOrElse(Left(EntityNotFoundError("Username does not exist!"))))
          .getOrElse(Left(EntityNotFoundError("User not found!")))
      )

  def getUserByEmailAndRealm(email: String, realm: String): Future[Either[DomainError, User]] =
    userRepository
      .getUserByEmailAndRealm(email, realm)
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

  def getPublicUserProfilesByIds(userIds: Seq[UUID]): Future[Seq[PublicUserProfile]] =
    userProfileRepository.getPublicUserProfilesByIds(userIds)

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

      publicUserProfile <- EitherT[Future, DomainError, PublicUserProfile](
        upsertPublicUserProfileIntoGraphDb(updatedUserProfileWithProfilePhoto.userId)
      )
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

      publicUserProfile <- EitherT[Future, DomainError, PublicUserProfile](
        upsertPublicUserProfileIntoGraphDb(updatedUserProfileWithProfilePhoto.userId)
      )
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

  def getUserTutorialStatus(userId: UUID): Future[Either[DomainError, UserTutorialStatus]] =
    userTutorialStatusRepository
      .getUserTutorialStatus(userId)
      .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User tutorial status not found!"))))

  def markUserTutorialStatusAsComplete(userId: UUID): Future[UserTutorialStatus] =
    userTutorialStatusRepository
      .markUserTutorialStatusAsComplete(userId)

  def markUserTutorialStatusAsIncomplete(userId: UUID): Future[UserTutorialStatus] =
    userTutorialStatusRepository
      .markUserTutorialStatusAsIncomplete(userId)

  def deleteUserTutorialStatus(userId: UUID): Future[Unit] =
    userTutorialStatusRepository
      .deleteUserTutorialStatus(userId)

  private def upsertPublicUserProfileIntoGraphDb(userId: UUID): Future[Either[DomainError, PublicUserProfile]] =
    (for {
      publicUserProfile <- EitherT[Future, DomainError, PublicUserProfile](
        userProfileRepository
          .getPublicUserProfileById(userId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("Public user profile not found!"))))
      )
      _ <- EitherT[Future, DomainError, Unit](socialService.upsertUser(publicUserProfile))
    } yield publicUserProfile).value

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
