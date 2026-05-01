-- system_config table for general configuration
CREATE TABLE IF NOT EXISTS system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(64) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(256),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
