# --- !Ups

CREATE TABLE "userPayment" (
   "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
   "userId" INTEGER NOT NULL,
   "paymentType" VARCHAR NOT NULL,
   "provider" VARCHAR NOT NULL,
   "accountNumber" VARCHAR NOT NULL,
   "expiry" DATE NOT NULL,
   FOREIGN KEY(userId) references user(id)

);

# --- !Downs
DROP TABLE "userPayment"