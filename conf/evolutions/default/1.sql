# --- !Ups

CREATE TABLE "user" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "nick" VARCHAR NOT NULL,
    "firstName" VARCHAR NOT NULL,
    "lastName" VARCHAR NOT NULL,
    "email" VARCHAR NOT NULL,
    "password" VARCHAR NOT NULL,
    "registeredAt" DATE,
    "lastLogin" DATE
);

# --- !Downs
DROP TABLE "user"