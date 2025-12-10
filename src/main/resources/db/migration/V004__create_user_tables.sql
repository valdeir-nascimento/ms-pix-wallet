CREATE TABLE user_account
(
    id         UUID PRIMARY KEY,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

CREATE TABLE user_account_roles
(
    user_account_id UUID        NOT NULL,
    role            VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_account_roles FOREIGN KEY (user_account_id) REFERENCES user_account (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_account_username ON user_account (username);
CREATE INDEX idx_user_account_roles_user ON user_account_roles (user_account_id);

