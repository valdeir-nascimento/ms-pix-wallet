-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE pix_key
(
    id         UUID PRIMARY KEY,
    wallet_id  UUID         NOT NULL REFERENCES wallet (id),
    key_type   VARCHAR(50)  NOT NULL,
    key_value  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);

ALTER TABLE pix_key
    ADD CONSTRAINT uk_pix_key_value UNIQUE (key_value);

CREATE INDEX idx_pix_key_wallet_type ON pix_key (wallet_id, key_type);
