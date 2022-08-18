# -- !Ups

-- Constraint table
alter table username_lock
    add column user_id uuid not null constraint not_null_constraint default '123e4567-e89b-12d3-a456-426614174000'::uuid,
    constraint fk_username_lock foreign key (user_id) references users(id) on delete cascade;

alter table username_lock
    drop constraint not_null_constraint;

# -- !Downs