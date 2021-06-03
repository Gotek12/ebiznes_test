# --- !Ups

CREATE TABLE "cart" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "userId" INTEGER NOT NULL,
    "createdAt" DATE NOT NULL,
    FOREIGN KEY(userId) references user(id)
);

# --- !Downs
DROP TABLE "cart"