-- Habilita funções para geração de UUID (gen_random_uuid)
-- CREATE
-- EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE wallet
(
    id              UUID PRIMARY KEY,
    owner_id        VARCHAR(255)             NOT NULL,
    current_balance NUMERIC(19, 2)           NOT NULL,
    status          VARCHAR(50)              NOT NULL,
    version         BIGINT                   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX uk_wallet_owner_id ON wallet (owner_id);
