# --- !Ups

CREATE TABLE "discount" (
        "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
        "name" VARCHAR NOT NULL,
        "discountPercentage" FLOAT NOT NULL,
        "expire" DATE NOT NULL,
        "productId" INTEGER NOT NULL,
        FOREIGN KEY(productId) references product(id)
);

# --- !Downs
DROP TABLE "discount"