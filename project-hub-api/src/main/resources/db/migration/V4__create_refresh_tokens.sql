CREATE TABLE refresh_tokens (
    token        VARCHAR(64)  PRIMARY KEY,
    username     VARCHAR(100) NOT NULL,
    expires_at   TIMESTAMP    NOT NULL,
    revoked      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_username ON refresh_tokens (username);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
