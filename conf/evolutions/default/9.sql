# --- !Ups

CREATE TABLE "productReview" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "productId" INTEGER NOT NULL,
    "date" DATE NOT NULL,
    "description" VARCHAR NOT NULL,
    "userId" INTEGER NOT NULL,
    FOREIGN KEY(productId) references product(id),
    FOREIGN KEY(userId) references user(id)
);

# --- !Downs
DROP TABLE "productReview"