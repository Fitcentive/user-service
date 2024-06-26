package io.fitcentive.user.domain

import play.api.libs.json.{JsString, Writes}

trait UserTrackingEvent {
  def stringValue: String
}

object UserTrackingEvent {

  def apply(status: String): UserTrackingEvent =
    status match {
      case ViewNotifications.stringValue                => ViewNotifications
      case ViewChatHome.stringValue                     => ViewChatHome
      case EnterChatRoom.stringValue                    => EnterChatRoom
      case AttemptToCreateChatRoom.stringValue          => AttemptToCreateChatRoom
      case CreateChatRoom.stringValue                   => CreateChatRoom
      case ViewDiaryHome.stringValue                    => ViewDiaryHome
      case SearchForExercise.stringValue                => SearchForExercise
      case SearchForFood.stringValue                    => SearchForFood
      case CreateFoodDiaryEntry.stringValue             => CreateFoodDiaryEntry
      case CreateExerciseDiaryEntry.stringValue         => CreateExerciseDiaryEntry
      case ViewDiaryEntry.stringValue                   => ViewDiaryEntry
      case EditDiaryEntry.stringValue                   => EditDiaryEntry
      case ViewDiaryDailySummary.stringValue            => ViewDiaryDailySummary
      case UpdateFitnessUserProfile.stringValue         => UpdateFitnessUserProfile
      case ViewMeetupHome.stringValue                   => ViewMeetupHome
      case AttemptToCreateMeetup.stringValue            => AttemptToCreateMeetup
      case CreateMeetup.stringValue                     => CreateMeetup
      case ViewDetailedMeetup.stringValue               => ViewDetailedMeetup
      case EditMeetup.stringValue                       => EditMeetup
      case RespondToMeetup.stringValue                  => RespondToMeetup
      case CommentOnMeetup.stringValue                  => CommentOnMeetup
      case AddAvailabilityToMeetup.stringValue          => AddAvailabilityToMeetup
      case AssociateDiaryEntryToMeetup.stringValue      => AssociateDiaryEntryToMeetup
      case ViewNewsfeedHome.stringValue                 => ViewNewsfeedHome
      case LikeSocialPost.stringValue                   => LikeSocialPost
      case AddSocialPostComment.stringValue             => AddSocialPostComment
      case AttemptToCreatePost.stringValue              => AttemptToCreatePost
      case CreatePost.stringValue                       => CreatePost
      case ViewCurrentUserAccountDetails.stringValue    => ViewCurrentUserAccountDetails
      case AttemptToActivatePremium.stringValue         => AttemptToActivatePremium
      case ActivatePremium.stringValue                  => ActivatePremium
      case EditCurrentUserAccountDetails.stringValue    => EditCurrentUserAccountDetails
      case ViewOtherUserProfile.stringValue             => ViewOtherUserProfile
      case SendFriendRequestToUser.stringValue          => SendFriendRequestToUser
      case AcceptUserFriendRequest.stringValue          => AcceptUserFriendRequest
      case DeclineUserFriendRequest.stringValue         => DeclineUserFriendRequest
      case UpdateDiscoveryPreferences.stringValue       => UpdateDiscoveryPreferences
      case AttemptToDiscoverUsers.stringValue           => AttemptToDiscoverUsers
      case AcceptNewDiscoveredUser.stringValue          => AcceptNewDiscoveredUser
      case ViewNewDiscoveredUser.stringValue            => ViewNewDiscoveredUser
      case RejectNewDiscoveredUser.stringValue          => RejectNewDiscoveredUser
      case RemoveFromNewlyDiscoveredUsers.stringValue   => RemoveFromNewlyDiscoveredUsers
      case UserLoggedIn.stringValue                     => UserLoggedIn
      case UserLoggedOut.stringValue                    => UserLoggedOut
      case LeaveChatRoom.stringValue                    => LeaveChatRoom
      case SearchForUsers.stringValue                   => SearchForUsers
      case ViewCalendar.stringValue                     => ViewCalendar
      case ViewFriends.stringValue                      => ViewFriends
      case CancelPremium.stringValue                    => CancelPremium
      case ViewAchievements.stringValue                 => ViewAchievements
      case ViewDetailedStepAchievements.stringValue     => ViewDetailedStepAchievements
      case ViewDetailedDiaryAchievements.stringValue    => ViewDetailedDiaryAchievements
      case ViewDetailedActivityAchievements.stringValue => ViewDetailedActivityAchievements
      case ViewDetailedWeightAchievements.stringValue   => ViewDetailedWeightAchievements
      case ViewProgress.stringValue                     => ViewProgress
      case ViewDetailedStepProgress.stringValue         => ViewDetailedStepProgress
      case ViewDetailedDiaryProgress.stringValue        => ViewDetailedDiaryProgress
      case ViewDetailedActivityProgress.stringValue     => ViewDetailedActivityProgress
      case ViewDetailedWeightProgress.stringValue       => ViewDetailedWeightProgress
      case UserAttemptedSharingMilestone.stringValue    => UserAttemptedSharingMilestone
      case UserSharedMilestone.stringValue              => UserSharedMilestone
      case UserDeletedSocialPost.stringValue            => UserDeletedSocialPost
      case _                                            => throw new Exception("Unexpected user tracking event")
    }

  implicit lazy val writes: Writes[UserTrackingEvent] = {
    {
      case ViewNotifications                => JsString(ViewNotifications.stringValue)
      case ViewChatHome                     => JsString(ViewChatHome.stringValue)
      case EnterChatRoom                    => JsString(EnterChatRoom.stringValue)
      case AttemptToCreateChatRoom          => JsString(AttemptToCreateChatRoom.stringValue)
      case CreateChatRoom                   => JsString(CreateChatRoom.stringValue)
      case ViewDiaryHome                    => JsString(ViewDiaryHome.stringValue)
      case SearchForExercise                => JsString(SearchForExercise.stringValue)
      case SearchForFood                    => JsString(SearchForFood.stringValue)
      case CreateFoodDiaryEntry             => JsString(CreateFoodDiaryEntry.stringValue)
      case CreateExerciseDiaryEntry         => JsString(CreateExerciseDiaryEntry.stringValue)
      case ViewDiaryEntry                   => JsString(ViewDiaryEntry.stringValue)
      case EditDiaryEntry                   => JsString(EditDiaryEntry.stringValue)
      case ViewDiaryDailySummary            => JsString(ViewDiaryDailySummary.stringValue)
      case UpdateFitnessUserProfile         => JsString(UpdateFitnessUserProfile.stringValue)
      case ViewMeetupHome                   => JsString(ViewMeetupHome.stringValue)
      case AttemptToCreateMeetup            => JsString(AttemptToCreateMeetup.stringValue)
      case CreateMeetup                     => JsString(CreateMeetup.stringValue)
      case ViewDetailedMeetup               => JsString(ViewDetailedMeetup.stringValue)
      case EditMeetup                       => JsString(EditMeetup.stringValue)
      case RespondToMeetup                  => JsString(RespondToMeetup.stringValue)
      case CommentOnMeetup                  => JsString(CommentOnMeetup.stringValue)
      case AddAvailabilityToMeetup          => JsString(AddAvailabilityToMeetup.stringValue)
      case AssociateDiaryEntryToMeetup      => JsString(AssociateDiaryEntryToMeetup.stringValue)
      case ViewNewsfeedHome                 => JsString(ViewNewsfeedHome.stringValue)
      case LikeSocialPost                   => JsString(LikeSocialPost.stringValue)
      case AddSocialPostComment             => JsString(AddSocialPostComment.stringValue)
      case AttemptToCreatePost              => JsString(AttemptToCreatePost.stringValue)
      case CreatePost                       => JsString(CreatePost.stringValue)
      case ViewCurrentUserAccountDetails    => JsString(ViewCurrentUserAccountDetails.stringValue)
      case AttemptToActivatePremium         => JsString(AttemptToActivatePremium.stringValue)
      case ActivatePremium                  => JsString(ActivatePremium.stringValue)
      case EditCurrentUserAccountDetails    => JsString(EditCurrentUserAccountDetails.stringValue)
      case ViewOtherUserProfile             => JsString(ViewOtherUserProfile.stringValue)
      case SendFriendRequestToUser          => JsString(SendFriendRequestToUser.stringValue)
      case AcceptUserFriendRequest          => JsString(AcceptUserFriendRequest.stringValue)
      case DeclineUserFriendRequest         => JsString(DeclineUserFriendRequest.stringValue)
      case UpdateDiscoveryPreferences       => JsString(UpdateDiscoveryPreferences.stringValue)
      case AttemptToDiscoverUsers           => JsString(AttemptToDiscoverUsers.stringValue)
      case ViewNewDiscoveredUser            => JsString(ViewNewDiscoveredUser.stringValue)
      case AcceptNewDiscoveredUser          => JsString(AcceptNewDiscoveredUser.stringValue)
      case RejectNewDiscoveredUser          => JsString(RejectNewDiscoveredUser.stringValue)
      case RemoveFromNewlyDiscoveredUsers   => JsString(RemoveFromNewlyDiscoveredUsers.stringValue)
      case UserLoggedIn                     => JsString(UserLoggedIn.stringValue)
      case UserLoggedOut                    => JsString(UserLoggedOut.stringValue)
      case LeaveChatRoom                    => JsString(LeaveChatRoom.stringValue)
      case SearchForUsers                   => JsString(SearchForUsers.stringValue)
      case ViewCalendar                     => JsString(ViewCalendar.stringValue)
      case ViewFriends                      => JsString(ViewFriends.stringValue)
      case CancelPremium                    => JsString(CancelPremium.stringValue)
      case ViewAchievements                 => JsString(ViewAchievements.stringValue)
      case ViewDetailedStepAchievements     => JsString(ViewDetailedStepAchievements.stringValue)
      case ViewDetailedDiaryAchievements    => JsString(ViewDetailedDiaryAchievements.stringValue)
      case ViewDetailedActivityAchievements => JsString(ViewDetailedActivityAchievements.stringValue)
      case ViewDetailedWeightAchievements   => JsString(ViewDetailedWeightAchievements.stringValue)
      case ViewProgress                     => JsString(ViewProgress.stringValue)
      case ViewDetailedStepProgress         => JsString(ViewDetailedStepProgress.stringValue)
      case ViewDetailedDiaryProgress        => JsString(ViewDetailedDiaryProgress.stringValue)
      case ViewDetailedActivityProgress     => JsString(ViewDetailedActivityProgress.stringValue)
      case ViewDetailedWeightProgress       => JsString(ViewDetailedWeightProgress.stringValue)
      case UserAttemptedSharingMilestone    => JsString(UserAttemptedSharingMilestone.stringValue)
      case UserSharedMilestone              => JsString(UserSharedMilestone.stringValue)
      case UserDeletedSocialPost            => JsString(UserDeletedSocialPost.stringValue)
    }
  }

  case object ViewNotifications extends UserTrackingEvent {
    val stringValue: String = "ViewNotifications"
  }
  case object ViewChatHome extends UserTrackingEvent {
    val stringValue: String = "ViewChatHome"
  }
  case object EnterChatRoom extends UserTrackingEvent {
    val stringValue: String = "EnterChatRoom"
  }
  case object AttemptToCreateChatRoom extends UserTrackingEvent {
    val stringValue: String = "AttemptToCreateChatRoom"
  }
  case object CreateChatRoom extends UserTrackingEvent {
    val stringValue: String = "CreateChatRoom"
  }
  case object ViewDiaryHome extends UserTrackingEvent {
    val stringValue: String = "ViewDiaryHome"
  }
  case object SearchForExercise extends UserTrackingEvent {
    val stringValue: String = "SearchForExercise"
  }
  case object SearchForFood extends UserTrackingEvent {
    val stringValue: String = "SearchForFood"
  }
  case object CreateFoodDiaryEntry extends UserTrackingEvent {
    val stringValue: String = "CreateFoodDiaryEntry"
  }
  case object CreateExerciseDiaryEntry extends UserTrackingEvent {
    val stringValue: String = "CreateExerciseDiaryEntry"
  }
  case object ViewDiaryEntry extends UserTrackingEvent {
    val stringValue: String = "ViewDiaryEntry"
  }
  case object EditDiaryEntry extends UserTrackingEvent {
    val stringValue: String = "EditDiaryEntry"
  }
  case object ViewDiaryDailySummary extends UserTrackingEvent {
    val stringValue: String = "ViewDiaryDailySummary"
  }
  case object UpdateFitnessUserProfile extends UserTrackingEvent {
    val stringValue: String = "UpdateFitnessUserProfile"
  }
  case object ViewMeetupHome extends UserTrackingEvent {
    val stringValue: String = "ViewMeetupHome"
  }
  case object AttemptToCreateMeetup extends UserTrackingEvent {
    val stringValue: String = "AttemptToCreateMeetup"
  }
  case object CreateMeetup extends UserTrackingEvent {
    val stringValue: String = "CreateMeetup"
  }
  case object ViewDetailedMeetup extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedMeetup"
  }
  case object EditMeetup extends UserTrackingEvent {
    val stringValue: String = "EditMeetup"
  }
  case object RespondToMeetup extends UserTrackingEvent {
    val stringValue: String = "RespondToMeetup"
  }
  case object CommentOnMeetup extends UserTrackingEvent {
    val stringValue: String = "CommentOnMeetup"
  }
  case object AddAvailabilityToMeetup extends UserTrackingEvent {
    val stringValue: String = "AddAvailabilityToMeetup"
  }
  case object AssociateDiaryEntryToMeetup extends UserTrackingEvent {
    val stringValue: String = "AssociateDiaryEntryToMeetup"
  }
  case object ViewNewsfeedHome extends UserTrackingEvent {
    val stringValue: String = "ViewNewsfeedHome"
  }
  case object LikeSocialPost extends UserTrackingEvent {
    val stringValue: String = "LikeSocialPost"
  }
  case object AddSocialPostComment extends UserTrackingEvent {
    val stringValue: String = "AddSocialPostComment"
  }
  case object AttemptToCreatePost extends UserTrackingEvent {
    val stringValue: String = "AttemptToCreatePost"
  }
  case object CreatePost extends UserTrackingEvent {
    val stringValue: String = "CreatePost"
  }
  case object ViewCurrentUserAccountDetails extends UserTrackingEvent {
    val stringValue: String = "ViewCurrentUserAccountDetails"
  }
  case object AttemptToActivatePremium extends UserTrackingEvent {
    val stringValue: String = "AttemptToActivatePremium"
  }
  case object ActivatePremium extends UserTrackingEvent {
    val stringValue: String = "ActivatePremium"
  }
  case object EditCurrentUserAccountDetails extends UserTrackingEvent {
    val stringValue: String = "EditCurrentUserAccountDetails"
  }
  case object ViewOtherUserProfile extends UserTrackingEvent {
    val stringValue: String = "ViewOtherUserProfile"
  }
  case object SendFriendRequestToUser extends UserTrackingEvent {
    val stringValue: String = "SendFriendRequestToUser"
  }
  case object AcceptUserFriendRequest extends UserTrackingEvent {
    val stringValue: String = "AcceptUserFriendRequest"
  }
  case object DeclineUserFriendRequest extends UserTrackingEvent {
    val stringValue: String = "DeclineUserFriendRequest"
  }
  case object UpdateDiscoveryPreferences extends UserTrackingEvent {
    val stringValue: String = "UpdateDiscoveryPreferences"
  }
  case object AttemptToDiscoverUsers extends UserTrackingEvent {
    val stringValue: String = "AttemptToDiscoverUsers"
  }
  case object ViewNewDiscoveredUser extends UserTrackingEvent {
    val stringValue: String = "ViewNewDiscoveredUser"
  }
  case object AcceptNewDiscoveredUser extends UserTrackingEvent {
    val stringValue: String = "AcceptNewDiscoveredUser"
  }
  case object RejectNewDiscoveredUser extends UserTrackingEvent {
    val stringValue: String = "RejectNewDiscoveredUser"
  }
  case object RemoveFromNewlyDiscoveredUsers extends UserTrackingEvent {
    val stringValue: String = "RemoveFromNewlyDiscoveredUsers"
  }
  case object UserLoggedIn extends UserTrackingEvent {
    val stringValue: String = "UserLoggedIn"
  }
  case object UserLoggedOut extends UserTrackingEvent {
    val stringValue: String = "UserLoggedOut"
  }
  case object LeaveChatRoom extends UserTrackingEvent {
    val stringValue: String = "LeaveChatRoom"
  }
  case object SearchForUsers extends UserTrackingEvent {
    val stringValue: String = "SearchForUsers"
  }
  case object ViewCalendar extends UserTrackingEvent {
    val stringValue: String = "ViewCalendar"
  }
  case object ViewFriends extends UserTrackingEvent {
    val stringValue: String = "ViewFriends"
  }
  case object CancelPremium extends UserTrackingEvent {
    val stringValue: String = "CancelPremium"
  }
  case object ViewAchievements extends UserTrackingEvent {
    val stringValue: String = "ViewAchievements"
  }
  case object ViewDetailedStepAchievements extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedStepAchievements"
  }
  case object ViewDetailedDiaryAchievements extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedDiaryAchievements"
  }
  case object ViewDetailedActivityAchievements extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedActivityAchievements"
  }
  case object ViewDetailedWeightAchievements extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedWeightAchievements"
  }
  case object ViewProgress extends UserTrackingEvent {
    val stringValue: String = "ViewProgress"
  }
  case object ViewDetailedStepProgress extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedStepProgress"
  }
  case object ViewDetailedDiaryProgress extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedDiaryProgress"
  }
  case object ViewDetailedActivityProgress extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedActivityProgress"
  }
  case object ViewDetailedWeightProgress extends UserTrackingEvent {
    val stringValue: String = "ViewDetailedWeightProgress"
  }
  case object UserAttemptedSharingMilestone extends UserTrackingEvent {
    val stringValue: String = "UserAttemptedSharingMilestone"
  }
  case object UserSharedMilestone extends UserTrackingEvent {
    val stringValue: String = "UserSharedMilestone"
  }
  case object UserDeletedSocialPost extends UserTrackingEvent {
    val stringValue: String = "UserDeletedSocialPost"
  }
}
