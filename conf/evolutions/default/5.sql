# --- !Ups

CREATE TABLE "product" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "name" VARCHAR NOT NULL,
    "description" VARCHAR NOT NULL,
    "categoryId" INTEGER NOT NULL,
    "price" decimal NOT NULL,
    "weight" DOUBLE,
    "height" DOUBLE,
    "width" DOUBLE,
    FOREIGN KEY(categoryId) references category(id)
);

# --- !Downs
DROP TABLE "product"