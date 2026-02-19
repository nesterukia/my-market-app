CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    img_path VARCHAR(255) NOT NULL,
    price BIGINT NOT NULL,
    CONSTRAINT items_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT orders_pkey PRIMARY KEY (id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGSERIAL NOT NULL,
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT order_item_pkey PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_item FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT carts_pkey PRIMARY KEY (id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGSERIAL NOT NULL,
    cart_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT cart_item_pkey PRIMARY KEY (id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
    CONSTRAINT fk_cart_item_item FOREIGN KEY (item_id) REFERENCES items(id)
);