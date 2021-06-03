# --- !Ups

CREATE TABLE "category" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL,
    "description" VARCHAR NOT NULL
);

# --- !Downs
DROP TABLE "category"