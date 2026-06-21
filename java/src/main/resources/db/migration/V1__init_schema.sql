CREATE TABLE IF NOT EXISTS partner (
    id          UUID        PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    document    VARCHAR(20)  NOT NULL UNIQUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS partner_credit (
    partner_id        UUID           PRIMARY KEY REFERENCES partner(id),
    credit_limit      NUMERIC(19, 4) NOT NULL CHECK (credit_limit >= 0),
    available_balance NUMERIC(19, 4) NOT NULL CHECK (available_balance >= 0),
    reserved_balance  NUMERIC(19, 4) NOT NULL DEFAULT 0 CONSTRAINT chk_reserved_balance_non_negative CHECK (reserved_balance >= 0),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS "order" (
    id           UUID           PRIMARY KEY,
    partner_id   UUID           NOT NULL REFERENCES partner(id),
    status       VARCHAR(30)    NOT NULL,
    total_amount NUMERIC(19, 4) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_order_partner_created ON "order" (partner_id, created_at);
CREATE INDEX IF NOT EXISTS idx_order_status_created  ON "order" (status, created_at);
CREATE INDEX IF NOT EXISTS idx_order_created_at      ON "order" (created_at DESC);

CREATE TABLE IF NOT EXISTS order_item (
    id         UUID           PRIMARY KEY,
    order_id   UUID           NOT NULL REFERENCES "order"(id),
    product_id VARCHAR(255)   NOT NULL,
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL CHECK (unit_price > 0)
);

CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_item(order_id);
