# --- !Ups
create table "USERS" ("ID" BIGSERIAL,"EMAIL" VARCHAR(254) NOT NULL PRIMARY KEY,"USERNAME" VARCHAR(254),"PASSWORD" BYTEA NOT NULL,"APPKEY" VARCHAR(254),"APPTOKEN" VARCHAR(254));

# --- !Downs
drop table "USERS";