-- CREATE
-- EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE pix_transfer
(
    id              UUID PRIMARY KEY,
    end_to_end_id   VARCHAR(255)   NOT NULL,
    from_wallet_id  UUID           NOT NULL,
    to_wallet_id    UUID           NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    status          VARCHAR(50)    NOT NULL,
    idempotency_key VARCHAR(255)   NOT NULL,
    created_at      TIMESTAMP    NOT NULL,

    CONSTRAINT fk_pix_transfer_from_wallet
        FOREIGN KEY (from_wallet_id) REFERENCES wallet (id),

    CONSTRAINT fk_pix_transfer_to_wallet
        FOREIGN KEY (to_wallet_id) REFERENCES wallet (id)
);

ALTER TABLE pix_transfer
    ADD CONSTRAINT uk_pix_transfer_end_to_end_id UNIQUE (end_to_end_id);


CREATE TABLE ledger_entry
(
    id                      UUID PRIMARY KEY,
    wallet_id               UUID           NOT NULL,
    end_to_end_id           VARCHAR(255),
    operation_type          VARCHAR(50)    NOT NULL,
    amount                  NUMERIC(19, 2) NOT NULL,
    balance_after_operation NUMERIC(19, 2) NOT NULL,
    occurred_at             TIMESTAMP    NOT NULL,

    CONSTRAINT fk_ledger_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallet (id)
);

CREATE INDEX idx_ledger_wallet_occurred_at
    ON ledger_entry (wallet_id, occurred_at);

CREATE TABLE pix_webhook_event
(
    id            UUID PRIMARY KEY,
    event_id      VARCHAR(255) NOT NULL,
    end_to_end_id VARCHAR(255) NOT NULL,
    event_type    VARCHAR(50)  NOT NULL,
    occurred_at   TIMESTAMP  NOT NULL,
    processed_at  TIMESTAMP  NOT NULL
);

ALTER TABLE pix_webhook_event
    ADD CONSTRAINT uk_pix_webhook_event_event_id UNIQUE (event_id);


CREATE TABLE idempotency_record
(
    id               UUID PRIMARY KEY,
    scope            VARCHAR(255)  NOT NULL,
    idempotency_key  VARCHAR(255)  NOT NULL,
    response_payload VARCHAR(4000) NOT NULL,
    created_at       TIMESTAMP   NOT NULL
);

ALTER TABLE idempotency_record
    ADD CONSTRAINT uk_idempotency_scope_key UNIQUE (scope, idempotency_key);
