# -- !Ups

alter table users
add column is_premium_enabled boolean not null default false ;
# -- !Downs