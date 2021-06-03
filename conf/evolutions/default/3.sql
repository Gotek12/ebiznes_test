# --- !Ups

CREATE TABLE "userAddress" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "userId" INTEGER NOT NULL,
    "city" VARCHAR NOT NULL,
    "postalCode" VARCHAR NOT NULL,
    "country" VARCHAR NOT NULL,
    "telephone" VARCHAR NOT NULL,
    "mobile" VARCHAR,
    "addressLine" VARCHAR NOT NULL,
    FOREIGN KEY(userId) references user(id)
);

# --- !Downs
DROP TABLE "userAddress"