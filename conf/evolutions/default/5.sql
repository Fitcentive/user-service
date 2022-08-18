# -- !Ups

-- Default to random UUID
alter table username_lock
    add column user_id uuid not null default uuid_generate_v4();

alter table username_lock
    add constraint fk_username_lock foreign key (user_id) references users(id) on delete cascade;

# -- !Downs