-- 복구 불가능한 OCI Object Storage / AWS S3 URL 정리
-- 실행 전 DB 백업 권장

-- photos: 외부 스토리지 URL 제거
UPDATE photos
SET image_url = NULL,
    thumbnail_url = NULL
WHERE image_url LIKE '%oraclecloud.com%'
   OR image_url LIKE '%amazonaws.com%'
   OR thumbnail_url LIKE '%oraclecloud.com%'
   OR thumbnail_url LIKE '%amazonaws.com%';

-- equipment
UPDATE equipment
SET image_url = NULL
WHERE image_url LIKE '%oraclecloud.com%'
   OR image_url LIKE '%amazonaws.com%';

-- news_images
DELETE FROM news_images
WHERE image_url LIKE '%oraclecloud.com%'
   OR image_url LIKE '%amazonaws.com%';

-- project_images
DELETE FROM project_images
WHERE image_url LIKE '%oraclecloud.com%'
   OR image_url LIKE '%amazonaws.com%';
