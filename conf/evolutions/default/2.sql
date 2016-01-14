# --- !Ups

create table public.users (
  userId TEXT PRIMARY KEY,
  providerid TEXT NOT NULL,
  providerkey TEXT NOT NULL,
  firstName TEXT,
  lastName TEXT,
  fullName TEXT,
  email TEXT,
  avatarURL TEXT
);

create table public.passwordinfo (
  hasher TEXT,
  password TEXT,
  salt TEXT,
  loginInfoId BIGINT
);

create table public.oauth1info (
  providerid TEXT NOT NULL,
  providerkey TEXT NOT NULL,
  token TEXT NOT NULL,
  secret TEXT NOT NULL
);

create table public.oauth2info (
  id BIGSERIAL PRIMARY KEY,
  accessToken TEXT,
  tokenType TEXT,
  expiresIn INTEGER,
  refreshToken TEXT,
  loginInfoId BIGINT
);

create table public.openidinfo(
  id TEXT PRIMARY KEY,
  loginInfoId BIGINT
);

create table public.openidattributes(
  id TEXT,
  key TEXT,
  value TEXT
);



# --- !Downs
drop table public.users;
drop table public.passwordinfo;
drop table public.oauth1info;
drop table public.oauth2info;
drop table public.openidinfo;
drop table public.openidattributes;