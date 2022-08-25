# -- !Ups

alter table user_agreements
    add column privacy_policy_accepted boolean not null default true;

# -- !Downs