CREATE TABLE IF NOT EXISTS order_item (
    id         UUID           PRIMARY KEY,
    order_id   UUID           NOT NULL REFERENCES "order"(id),
    product_id VARCHAR(255)   NOT NULL,
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL CHECK (unit_price > 0)
);

CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);
