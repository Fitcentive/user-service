# -- !Ups

create table user_agreements (
    user_id uuid not null constraint user_agreements primary key,
    terms_and_conditions_accepted varchar,
    subscribe_to_emails varchar,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_user foreign key (user_id) references users(id) on delete cascade
);

# -- !Downs
