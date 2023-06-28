# -- !Ups

-- Constraint tables
create table user_event_types (
    name varchar not null constraint pk_user_event_types primary key,
    description varchar not null
);

insert into user_event_types (name, description) values ('ViewNotifications',               'When a user views their notifications via the app');
insert into user_event_types (name, description) values ('ViewChatHome',                    'When a user views their chat home view via the app');
insert into user_event_types (name, description) values ('EnterChatRoom',                   'When a user enters a chat room');
insert into user_event_types (name, description) values ('AttemptToCreateChatRoom',         'When a user tries to create a chat room');
insert into user_event_types (name, description) values ('CreateChatRoom',                  'When a user creates a chat room');
insert into user_event_types (name, description) values ('ViewDiaryHome',                   'When a user views their diary homepage');
insert into user_event_types (name, description) values ('SearchForExercise',               'When a user searches for an exercise');
insert into user_event_types (name, description) values ('SearchForFood',                   'When a user searches for food');
insert into user_event_types (name, description) values ('CreateFoodDiaryEntry',            'When a user enters a food diary entry');
insert into user_event_types (name, description) values ('CreateExerciseDiaryEntry',        'When a user enters am exercise diary entry');
insert into user_event_types (name, description) values ('ViewDiaryEntry',                  'When a user views a diary entry');
insert into user_event_types (name, description) values ('EditDiaryEntry',                  'When a user edits a viewed diary entry');
insert into user_event_types (name, description) values ('UpdateFitnessUserProfile',        'When a user updates their fitness profile');
insert into user_event_types (name, description) values ('ViewMeetupHome',                  'When a user views their meetup homepage');
insert into user_event_types (name, description) values ('AttemptToCreateMeetup',           'When a user tries to create a meetup');
insert into user_event_types (name, description) values ('CreateMeetup',                    'When a user creates a meetup');
insert into user_event_types (name, description) values ('ViewDetailedMeetup',              'When a user views a meetup in detail');
insert into user_event_types (name, description) values ('EditMeetup',                      'When a user edits a meetup');
insert into user_event_types (name, description) values ('RespondToMeetup',                 'When a user responds to a meetup');
insert into user_event_types (name, description) values ('CommentOnMeetup',                 'When a user comments on a meetup');
insert into user_event_types (name, description) values ('AddAvailabilityToMeetup',         'When a user adds their availability to a meetup');
insert into user_event_types (name, description) values ('AssociateDiaryEntryToMeetup',     'When a user associates a diary entry to a meetup');
insert into user_event_types (name, description) values ('ViewNewsfeedHome',                'When a user views their newsfeed home');
insert into user_event_types (name, description) values ('LikeSocialPost',                  'When a user likes a post');
insert into user_event_types (name, description) values ('AddSocialPostComment',            'When a user comments on a post');
insert into user_event_types (name, description) values ('AttemptToCreatePost',             'When a user tries to create a post');
insert into user_event_types (name, description) values ('CreatePost',                      'When a user creates a post');
insert into user_event_types (name, description) values ('ViewCurrentUserAccountDetails',   'When a user views their own account details');
insert into user_event_types (name, description) values ('AttemptToActivatePremium',        'When a user attempts to activate premium');
insert into user_event_types (name, description) values ('ActivatePremium',                 'When a user activates premium');
insert into user_event_types (name, description) values ('EditCurrentUserAccountDetails',   'When a user edits their own account details');
insert into user_event_types (name, description) values ('ViewOtherUserProfile',            'When a user views another user profile');
insert into user_event_types (name, description) values ('SendFriendRequestToUser',         'When a user sends a friend request to another user');
insert into user_event_types (name, description) values ('AcceptUserFriendRequest',         'When a user accepts a friend request from another user');
insert into user_event_types (name, description) values ('DeclineUserFriendRequest',        'When a user declines a friend request from another user');
insert into user_event_types (name, description) values ('UpdateDiscoveryPreferences',      'When a user updates their discovery preferences');
insert into user_event_types (name, description) values ('AttemptToDiscoverUsers',          'When a user tries to discover users');
insert into user_event_types (name, description) values ('AcceptNewDiscoveredUser',         'When a user accepts a newly discovered user');
insert into user_event_types (name, description) values ('RejectNewDiscoveredUser',         'When a user rejects a newly discovered user');
insert into user_event_types (name, description) values ('RemoveFromNewlyDiscoveredUsers',  'When a user removes a newly discovered user from their list of discovered users');

create table event_platform_types (
    name varchar not null constraint pk_event_platform_types primary key,
    description varchar not null
);

insert into event_platform_types (name, description) values ('Android',  'Accessed from Android app');
insert into event_platform_types (name, description) values ('iOS',      'Accessed from iOS app');
insert into event_platform_types (name, description) values ('Web',      'Accessed from web app');


create table user_event_tracking (
    event_id uuid not null constraint pk_user_event_tracking primary key,
    user_id uuid not null,
    event_name varchar not null constraint fk_user_event_types references user_event_types,
    event_platform varchar not null constraint fk_event_platform_types references event_platform_types,
    created_at timestamp not null default now(),
    constraint fk_user_event_tracking foreign key (user_id) references users(id) on delete cascade
);

# -- !Downs