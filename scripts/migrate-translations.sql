-- Additive and repeatable. No existing content is queued by this migration.
CREATE TABLE IF NOT EXISTS content_translations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    kind VARCHAR(32) NOT NULL,
    content_id BIGINT NOT NULL,
    field_path VARCHAR(128) NOT NULL,
    source_hash VARCHAR(64) NOT NULL,
    source_text LONGTEXT,
    source_language VARCHAR(2),
    text_en LONGTEXT,
    text_de LONGTEXT,
    manual_en BIT NOT NULL DEFAULT 0,
    manual_de BIT NOT NULL DEFAULT 0,
    state VARCHAR(16) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    next_attempt DATETIME(6) NOT NULL,
    error_code VARCHAR(40),
    version BIGINT,
    CONSTRAINT uq_translation_field UNIQUE (kind, content_id, field_path),
    INDEX idx_translation_work (state, next_attempt)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
