# --- !Ups

create table public.users (
  userId TEXT PRIMARY KEY,
  firstName TEXT,
  lastName TEXT,
  fullName TEXT,
  email TEXT,
  avatarURL TEXT
);


create table public.logininfo (
  id BIGSERIAL PRIMARY KEY,
  providerId TEXT,
  providerKey TEXT
);

create table public.userlogininfo (
  userId TEXT,
  loginInfoId BIGINT
);

create table public.passwordinfo (
  hasher TEXT,
  password TEXT,
  salt TEXT,
  loginInfoId BIGINT
);

create table public.oauth1info (
  id BIGINT PRIMARY KEY ,
  token TEXT,
  secret TEXT,
  loginInfoId BIGINT
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
drop table public.logininfo;
drop table public.userlogininfo;
drop table public.oauth1info;
drop table public.oauth2info;
drop table public.openidinfo;
drop table public.openidattributes;