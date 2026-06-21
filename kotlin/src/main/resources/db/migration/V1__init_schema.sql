CREATE TABLE IF NOT EXISTS partner (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    document    VARCHAR(20)  NOT NULL UNIQUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS partner_balance (
    partner_id        UUID PRIMARY KEY REFERENCES partner(id),
    total_credited    NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (total_credited >= 0),
    total_debited     NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (total_debited >= 0),
    available_balance NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK (available_balance >= 0),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS transaction (
    id                UUID PRIMARY KEY,
    partner_id        UUID NOT NULL REFERENCES partner(id),
    type              VARCHAR(10) NOT NULL CHECK (type IN ('CREDIT','DEBIT')),
    amount            NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    description       TEXT NOT NULL,
    idempotency_key   VARCHAR(255) NOT NULL UNIQUE,
    status            VARCHAR(10) NOT NULL CHECK (status IN ('PENDING','COMPLETED','FAILED')),
    error_description TEXT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_partner_created_at ON partner(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_partner_created ON transaction(partner_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_partner_type_created ON transaction(partner_id, type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_transaction_status_created ON transaction(status, created_at ASC);
