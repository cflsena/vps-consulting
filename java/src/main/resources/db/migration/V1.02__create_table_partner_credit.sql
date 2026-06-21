CREATE TABLE IF NOT EXISTS partner_credit (
    id UUID           PRIMARY KEY REFERENCES partner(id),
    credit_limit      NUMERIC(19, 4) NOT NULL CHECK (credit_limit >= 0),
    available_balance NUMERIC(19, 4) NOT NULL CHECK (available_balance >= 0),
    reserved_balance  NUMERIC(19, 4) NOT NULL DEFAULT 0 CONSTRAINT chk_reserved_balance_non_negative CHECK (reserved_balance >= 0),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

ALTER TABLE partner_credit
    ADD CONSTRAINT FK_PARTNER_CREDIT_ID_PARTNER_ID FOREIGN KEY (id) REFERENCES partner (id);