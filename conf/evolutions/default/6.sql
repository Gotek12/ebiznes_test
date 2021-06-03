# --- !Ups

CREATE TABLE "cartItem" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "quantity" INTEGER NOT NULL,
    "productId" INTEGER NOT NULL,
    "cartId" INTEGER NOT NULL,
    FOREIGN KEY(cartId) references cart(id),
    FOREIGN KEY(productId) references product(id)
);

# --- !Downs
DROP TABLE "cartItem"