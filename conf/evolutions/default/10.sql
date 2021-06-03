# --- !Ups

CREATE TABLE "order" (
   "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
   "userId" INTEGER NOT NULL,
   "status" INTEGER NOT NULL,
   "items" INTEGER NOT NULL,
   "createdAt" DATE NOT NULL,
   "tax" FLOAT NOT NULL,
   "price" decimal not null,
   "shipping" FLOAT,
   "payed" bit not null,
   "realized" bit not null,
   FOREIGN KEY(userId) references user(id)
);

# --- !Downs
DROP TABLE "order"