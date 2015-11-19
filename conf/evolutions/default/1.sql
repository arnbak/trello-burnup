# --- !Ups

create table public.boardperiods (
  id BIGSERIAL PRIMARY KEY,
  boardid VARCHAR(254) NOT NULL,
  startdate BIGINT NOT NULL,
  enddate BIGINT NOT NULL,
  periodindays INTEGER NOT NULL
);

create table public.dailypoints (
  id BIGSERIAL PRIMARY KEY,
  boardid VARCHAR(254) NOT NULL,
  date BIGINT NOT NULL,
  scope INTEGER NOT NULL,
  progress INTEGER NOT NULL,
  finished INTEGER NOT NULL
);

# --- !Downs

drop table public.dailypoints;
drop table public.boardperiods;

