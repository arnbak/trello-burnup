# --- !Ups

create table public.boardperiods (
  id BIGSERIAL PRIMARY KEY,
  boardid TEXT NOT NULL,
  startdate TIMESTAMP NOT NULL,
  enddate TIMESTAMP NOT NULL,
  periodindays INTEGER NOT NULL
);

create table public.dailypoints (
  id BIGSERIAL PRIMARY KEY,
  boardid TEXT NOT NULL,
  date TIMESTAMP NOT NULL,
  scope INTEGER NOT NULL,
  progress INTEGER NOT NULL,
  finished INTEGER NOT NULL
);

# --- !Downs

drop table public.dailypoints;
drop table public.boardperiods;

