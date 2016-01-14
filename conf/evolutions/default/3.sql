# --- !Ups

create table public.boards (
    boardId TEXT PRIMARY KEY NOT NULL,
    boardName TEXT NOT NULL,
    selected BOOLEAN,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs

drop table public.boards;