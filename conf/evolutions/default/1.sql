# -- !Ups

-- To ensure UUIDs are autogenerated
create extension if not exists "uuid-ossp";

-- To support geography/geometry type
create extension if not exists "postgis";

-- Constraint tables
create table account_status_types (
    name varchar not null constraint pk_account_status_type primary key,
    description varchar not null
);

insert into account_status_types (name, description) values ('UsernameCreationRequired',    'User yet to create username');
insert into account_status_types (name, description) values ('ProfileInfoRequired',         'User yet to provide details');
insert into account_status_types (name, description) values ('TermsAndConditionsRequired',  'User yet to accept terms and conditions');
insert into account_status_types (name, description) values ('LoginReady',                  'User ready to login');
insert into account_status_types (name, description) values ('LocationRadiusRequired',      'User yet to provide location of interest');

-- Constraint tables
create table auth_provider_types (
    name varchar not null constraint pk_auth_provider_type primary key,
    description varchar not null
);

insert into auth_provider_types (name, description) values ('NativeAuth',    'Authentication using username/password');
insert into auth_provider_types (name, description) values ('GoogleAuth',    'Authentication using Google SSO');
insert into auth_provider_types (name, description) values ('AppleAuth',     'Authentication using Apple SSO');
insert into auth_provider_types (name, description) values ('FacebookAuth',  'Authentication using Facebook SSO');

-- Constraint table
create table gender_types (
    name varchar not null constraint pk_gender_types primary key
);

insert into gender_types (name) values ('Male');
insert into gender_types (name) values ('Female');
insert into gender_types (name) values ('Other');

create table users (
    id uuid not null constraint pk_user primary key,
    email varchar not null unique,
    username varchar unique,
    account_status varchar not null constraint fk_account_type references account_status_types,
    auth_provider varchar not null constraint fk_auth_provider_type references auth_provider_types,
    enabled boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table user_profiles (
    user_id uuid not null constraint pk_user_profile primary key,
    first_name varchar,
    last_name varchar,
    photo_url varchar,
    date_of_birth date,
    location_center geometry(Point, 4326),
    location_radius integer,
    gender varchar constraint fk_gender_types references gender_types (name),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_user foreign key (user_id) references users(id) on delete cascade
);

create table email_verification_tokens (
    email varchar not null,
    token varchar not null,
    expiry bigint not null,
    primary key (email, token)
);

create table username_lock (
    username varchar not null constraint pk_username_lock primary key,
    user_id uuid not null,
    constraint fk_username_lock foreign key (user_id) references users(id) on delete cascade
);

create table user_agreements (
    user_id uuid not null constraint pk_user_agreements primary key,
    terms_and_conditions_accepted boolean not null,
    subscribe_to_emails boolean not null,
    privacy_policy_accepted boolean not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_user foreign key (user_id) references users(id) on delete cascade
);


create table user_friend_requests(
    requesting_user_id uuid not null,
    target_user_id uuid not null,
    created_at timestamp not null default now(),
    primary key (requesting_user_id, target_user_id),
    constraint fk_requesting_user_id foreign key (requesting_user_id) references users(id) on delete cascade,
    constraint fk_target_user_id foreign key (target_user_id) references users(id) on delete cascade
);
# -- !Downs