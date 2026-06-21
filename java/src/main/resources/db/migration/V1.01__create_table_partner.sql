CREATE TABLE IF NOT EXISTS partner (
    id          UUID        PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    document    VARCHAR(20)  NOT NULL UNIQUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
