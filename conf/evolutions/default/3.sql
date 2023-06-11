# -- !Ups

create table user_tutorial_status (
    user_id uuid not null constraint pk_user_tutorial_status primary key,
    is_tutorial_complete boolean not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_user foreign key (user_id) references users(id) on delete cascade
);

# -- !Downs