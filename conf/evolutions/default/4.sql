# -- !Ups

-- Constraint table
create table gender_types (
    name varchar not null constraint pk_gender_types primary key
);

insert into gender_types (name) values ('Male');
insert into gender_types (name) values ('Female');
insert into gender_types (name) values ('Other');

alter table user_profiles
    add column gender varchar constraint fk_gender_types references gender_types (name);

# -- !Downs