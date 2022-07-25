# -- !Ups

-- To support geography/geometry type
create extension if not exists "postgis";


-- points in geographic wgs84 coordinates (epsg:4326)
alter table user_profiles
    add column location_center geometry(Point, 4326),
    add column location_radius integer;


insert into account_status_types (name, description) values ('LocationRadiusRequired',  'User yet to provide location of interest');

# -- !Downs