"""Run real SQL twice on legacy synthetic rows, before starting Hibernate."""
from pathlib import Path
import runpy
import subprocess

ROOT = Path(__file__).resolve().parents[1]
COMPOSE = runpy.run_path(str(ROOT / 'scripts/test-stack.py'))['COMPOSE']


def sql(statement):
    result = subprocess.run([*COMPOSE, 'exec', '-T', 'db', 'mysql', '-u', 'root',
                             '-ptest-root-only', '--batch', '--skip-column-names',
                             '--default-character-set=utf8mb4', 'twiiiins_test'],
                            input=statement, text=True, encoding='utf-8', capture_output=True, check=True)
    return result.stdout.strip()


def main():
    try:
        subprocess.run([*COMPOSE, 'up', '-d', '--wait', '--wait-timeout', '180', 'db'], check=True)
        sql("""CREATE TABLE news (id BIGINT AUTO_INCREMENT PRIMARY KEY, date DATE NOT NULL,
             title VARCHAR(255) NOT NULL, description VARCHAR(1000), display_order INT);
             CREATE TABLE news_images (news_id BIGINT NOT NULL, image_url VARCHAR(2048));
             INSERT INTO news VALUES (101,'2026-10-31','Legacy Grüße','Hello <world> & friends',0);
             INSERT INTO news_images VALUES (101,'/uploads/b.png'),(101,'/uploads/a.png');""")
        for _ in range(2):
            for name in ['migrate-newsletter.sql', 'migrate-translations.sql']:
                sql((ROOT / 'scripts' / name).read_text(encoding='utf-8'))
            assert sql('SELECT title FROM news WHERE id=101') == 'Legacy Grüße'
            assert sql('SELECT source FROM news WHERE id=101') == 'NEWS'
            assert sql('SELECT body_en FROM news WHERE id=101') == '<p>Hello &lt;world&gt; &amp; friends</p>'
            assert sql('SELECT COUNT(*) FROM news_images WHERE news_id=101') == '2'
            assert sql('SELECT COUNT(*) FROM content_translations') == '0'
            assert sql("SELECT COLUMN_TYPE FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='content_translations' AND column_name='source_text'") == 'longtext'
            assert sql("SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='content_translations' AND index_name='uq_translation_field' AND non_unique=0") == '3'
            assert sql("SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='newsletter_deliveries' AND index_name='newsletter_recipient' AND non_unique=0") == '2'
        # Administrator ordering and explicit source must survive later migrations.
        sql("UPDATE news SET source='NEWS', title_de='Manuell'; UPDATE news_images SET image_order=9 WHERE image_url='/uploads/a.png';")
        sql((ROOT / 'scripts/migrate-newsletter.sql').read_text(encoding='utf-8'))
        assert sql('SELECT source FROM news WHERE id=101') == 'NEWS'
        assert sql("SELECT image_order FROM news_images WHERE image_url='/uploads/a.png'") == '9'
        print('Legacy data, escaping, source classification and migration idempotency passed.')
    finally:
        subprocess.run([*COMPOSE, 'down', '-v', '--remove-orphans'], check=True)


if __name__ == '__main__':
    main()
