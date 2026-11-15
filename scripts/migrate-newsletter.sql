-- MySQL 8. Run with the application stopped and after a database backup.
-- Additive and re-runnable; existing published News and image rows are preserved.
CREATE TABLE IF NOT EXISTS news (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, date DATE NOT NULL, title VARCHAR(255) NOT NULL,
  description VARCHAR(1000), display_order INT
);
CREATE TABLE IF NOT EXISTS news_images (news_id BIGINT NOT NULL, image_url VARCHAR(2048));
ALTER TABLE news_images MODIFY COLUMN image_url VARCHAR(2048);
DELIMITER $$
DROP PROCEDURE IF EXISTS newsletter_add_column$$
CREATE PROCEDURE newsletter_add_column(IN table_name_arg VARCHAR(64), IN column_name_arg VARCHAR(64), IN definition_arg TEXT)
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = table_name_arg AND column_name = column_name_arg) THEN
    SET @newsletter_ddl = CONCAT('ALTER TABLE `', table_name_arg, '` ADD COLUMN `', column_name_arg, '` ', definition_arg);
    PREPARE newsletter_statement FROM @newsletter_ddl;
    EXECUTE newsletter_statement;
    DEALLOCATE PREPARE newsletter_statement;
  END IF;
END$$
DELIMITER ;
CALL newsletter_add_column('news', 'title_de', 'VARCHAR(255) NULL');
CALL newsletter_add_column('news', 'body_en', 'LONGTEXT NULL');
CALL newsletter_add_column('news', 'body_de', 'LONGTEXT NULL');
CALL newsletter_add_column('news', 'status', 'VARCHAR(20) NOT NULL DEFAULT ''PUBLISHED''');
CALL newsletter_add_column('news', 'archived', 'BIT NOT NULL DEFAULT b''0''');
CALL newsletter_add_column('news', 'version', 'BIGINT NULL DEFAULT 0');
SET @newsletter_classify_sources = NOT EXISTS (
  SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'news' AND column_name = 'source'
);
CALL newsletter_add_column('news', 'source', 'VARCHAR(20) NOT NULL DEFAULT ''NEWS''');
CALL newsletter_add_column('news_images', 'image_order', 'INT NULL');
CALL newsletter_add_column('news', 'event_when_en', 'VARCHAR(255) NULL');
CALL newsletter_add_column('news', 'event_when_de', 'VARCHAR(255) NULL');
CALL newsletter_add_column('news', 'event_location_en', 'VARCHAR(255) NULL');
CALL newsletter_add_column('news', 'event_location_de', 'VARCHAR(255) NULL');
CALL newsletter_add_column('news', 'cta_label_en', 'VARCHAR(255) NULL');
CALL newsletter_add_column('news', 'cta_label_de', 'VARCHAR(255) NULL');
CALL newsletter_add_column('news', 'cta_url', 'VARCHAR(2048) NULL');

CREATE TABLE IF NOT EXISTS news_videos (
  news_id BIGINT NOT NULL, video_order INT NOT NULL, video_url VARCHAR(2048),
  PRIMARY KEY (news_id, video_order), FOREIGN KEY (news_id) REFERENCES news(id)
);
CREATE TABLE IF NOT EXISTS admin_sessions (
  token_hash VARCHAR(255) PRIMARY KEY, username VARCHAR(255) NOT NULL, expires_at DATETIME(6) NOT NULL
);
CREATE TABLE IF NOT EXISTS newsletter_gate (
  id BIGINT PRIMARY KEY, paused BIT NOT NULL DEFAULT b'0', reason VARCHAR(255)
);
INSERT IGNORE INTO newsletter_gate (id, paused) VALUES (1, b'0');
CREATE TABLE IF NOT EXISTS newsletter_subscribers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(254) NOT NULL UNIQUE,
  language VARCHAR(2) NOT NULL, pending_language VARCHAR(2), status VARCHAR(20) NOT NULL,
  confirmation_hash VARCHAR(64), confirmation_context VARCHAR(255), confirmation_expires DATETIME(6),
  requested_at DATETIME(6), created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL,
  consent_at DATETIME(6), consent_version VARCHAR(255), unsubscribe_version VARCHAR(255)
);
CALL newsletter_add_column('newsletter_subscribers', 'email_key',
  'VARCHAR(254) NULL, ADD UNIQUE KEY newsletter_email_key (email_key)');
CALL newsletter_add_column('newsletter_subscribers', 'status_before_exclusion', 'VARCHAR(20) NULL');
CALL newsletter_add_column('newsletter_subscribers', 'unsubscribed_at', 'DATETIME(6) NULL');
-- Leave historical collisions intact, including their IDs, consent and unsubscribe links.
-- The application checks these rows before registering another spelling of the same address.
CREATE TEMPORARY TABLE newsletter_email_backfill AS
  SELECT MIN(id) AS id, email_key FROM (
    SELECT id, CASE WHEN RIGHT(LOWER(TRIM(email)), 10) = '@gmail.com'
      THEN CONCAT(REPLACE(LEFT(LOWER(TRIM(email)), CHAR_LENGTH(TRIM(email)) - 10), '.', ''), '@gmail.com')
      ELSE LOWER(TRIM(email)) END AS email_key
    FROM newsletter_subscribers
  ) AS normalized GROUP BY email_key HAVING COUNT(*) = 1;
UPDATE newsletter_subscribers s JOIN newsletter_email_backfill b ON b.id = s.id
  SET s.email_key = b.email_key WHERE s.email_key IS NULL;
DROP TEMPORARY TABLE newsletter_email_backfill;
CREATE TABLE IF NOT EXISTS newsletter_mailings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, news_id BIGINT NOT NULL UNIQUE, created_at DATETIME(6),
  title_en VARCHAR(255) NOT NULL, title_de VARCHAR(255) NOT NULL,
  html_en LONGTEXT, html_de LONGTEXT, images LONGTEXT,
  paused BIT NOT NULL DEFAULT b'0', pause_reason VARCHAR(255),
  accepted_count INT NOT NULL DEFAULT 0, failed_count INT NOT NULL DEFAULT 0,
  skipped_count INT NOT NULL DEFAULT 0, unknown_count INT NOT NULL DEFAULT 0, total_count INT NOT NULL DEFAULT 0
);
CALL newsletter_add_column('newsletter_mailings', 'publish_to_news', 'BIT NOT NULL DEFAULT b''1''');
DROP PROCEDURE newsletter_add_column;
CREATE TABLE IF NOT EXISTS newsletter_deliveries (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, mailing_id BIGINT, subscriber_id BIGINT,
  kind VARCHAR(20) NOT NULL, email VARCHAR(254), language VARCHAR(2), status VARCHAR(20) NOT NULL,
  confirmation_context VARCHAR(255), subscription_version VARCHAR(255), test_html LONGTEXT, test_title VARCHAR(255),
  attempts INT NOT NULL DEFAULT 0, error_code VARCHAR(255), created_at DATETIME(6),
  next_attempt DATETIME(6), attempted_at DATETIME(6), finished_at DATETIME(6),
  UNIQUE KEY newsletter_recipient (mailing_id, subscriber_id),
  INDEX newsletter_pending (status, next_attempt), INDEX newsletter_attempted (attempted_at)
);
CREATE TABLE IF NOT EXISTS newsletter_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, delivery_id BIGINT, attempted_at DATETIME(6),
  INDEX newsletter_attempt_time (attempted_at)
);

START TRANSACTION;
-- Classify content from the earlier combined editor once; preserve explicitly assigned sources on later runs.
UPDATE news SET source = 'NEWSLETTER' WHERE @newsletter_classify_sources AND (
  status = 'DRAFT' OR NULLIF(TRIM(body_de), '') IS NOT NULL OR id IN (SELECT news_id FROM newsletter_mailings)
);
UPDATE news SET version = 0 WHERE version IS NULL;
UPDATE news SET body_en = CONCAT('<p>', REPLACE(REPLACE(REPLACE(REPLACE(COALESCE(description, ''), '&', '&amp;'), '<', '&lt;'), '>', '&gt;'), CHAR(10), '<br>'), '</p>') WHERE body_en IS NULL;

-- Only unindexed legacy collections are reordered. Repeated runs keep administrator ordering.
CREATE TEMPORARY TABLE newsletter_legacy_images AS
  SELECT news_id, image_url, ROW_NUMBER() OVER (PARTITION BY news_id ORDER BY image_url) - 1 AS image_order
  FROM news_images WHERE news_id IN (SELECT legacy.news_id FROM (SELECT DISTINCT news_id FROM news_images WHERE image_order IS NULL) legacy);
DELETE FROM news_images WHERE news_id IN (SELECT news_id FROM newsletter_legacy_images);
INSERT INTO news_images (news_id, image_url, image_order) SELECT news_id, image_url, image_order FROM newsletter_legacy_images;
DROP TEMPORARY TABLE newsletter_legacy_images;
COMMIT;
