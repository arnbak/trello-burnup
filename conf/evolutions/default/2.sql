# --- !Ups
create table public.users (
  id BIGSERIAL,
  email VARCHAR(254) NOT NULL PRIMARY KEY,
  username VARCHAR(254),
  password BYTEA NOT NULL,
  appkey VARCHAR(254),
  apptoken VARCHAR(254)
);

# --- !Downs
drop table public.users;