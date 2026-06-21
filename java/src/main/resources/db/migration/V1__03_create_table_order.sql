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
