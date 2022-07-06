# -- !Ups
create table user_follow_requests(
    requesting_user_id uuid not null,
    target_user_id uuid not null,
    created_at timestamp not null default now(),
    primary key (requesting_user_id, target_user_id),
    constraint fk_requesting_user_id foreign key (requesting_user_id) references users(id) on delete cascade,
    constraint fk_target_user_id foreign key (target_user_id) references users(id) on delete cascade
);


# -- !Downs