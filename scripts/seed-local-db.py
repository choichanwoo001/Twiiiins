"""Only the dedicated MySQL process started by start-local.ps1 is supported."""
import argparse
import os
from pathlib import Path
import re
import shutil
import subprocess

ROOT = Path(__file__).resolve().parent.parent
CLIENT = Path.home() / '.cache/twiiiins-local-runtime/mysql/mysql-8.0.43-winx64/bin/mysql.exe'
DATABASE = 'twiiiins_local'
PASSWORD = 'twiiiins-local-root'

def sql(query, *, database=False, password=PASSWORD, check=True):
    args = [str(CLIENT), '--no-defaults', '--protocol=TCP', '--host=127.0.0.1', '--port=3307', '--user=root', '--default-character-set=utf8mb4', '--batch', '--skip-column-names']
    if database:
        args += ['--database=' + DATABASE]
    result = subprocess.run(args, input=query, text=True, encoding='utf-8', capture_output=True,
                            env={**os.environ, 'MYSQL_PWD': password})
    if check and result.returncode:
        raise RuntimeError(result.stderr.strip())
    return result

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--bootstrap-only', action='store_true')
    options = parser.parse_args()
    probe = sql('SELECT @@datadir;', check=False)
    first_start = probe.returncode != 0
    if first_start:
        probe = sql('SELECT @@datadir;', password='')
    if Path(probe.stdout.strip()).resolve() != (ROOT / '.local/mysql-data').resolve():
        raise RuntimeError('This is not the dedicated project-local MySQL data directory.')
    if first_start:
        sql("ALTER USER 'root'@'localhost' IDENTIFIED BY 'twiiiins-local-root';", password='')
    sql("CREATE DATABASE IF NOT EXISTS twiiiins_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; "
        "CREATE USER IF NOT EXISTS 'twiiiins_local'@'localhost' IDENTIFIED BY 'twiiiins-local-dev'; "
        "GRANT ALL PRIVILEGES ON twiiiins_local.* TO 'twiiiins_local'@'localhost';")
    if options.bootstrap_only:
        print('Local database ready: 127.0.0.1:3307/twiiiins_local')
    else:
        seed = (ROOT / 'scripts/local-seed.sql').read_text(encoding='utf-8')
        tables = sorted(set(re.findall(r'INSERT INTO `([^`]+)`', seed)))
        counts = sql(' UNION ALL '.join(f"SELECT '{table}', COUNT(*) FROM `{table}`" for table in tables) + ';', database=True).stdout
        if any(int(line.split('\t')[1]) for line in counts.strip().splitlines()):
            print('Existing local data kept; seed import skipped.')
        else:
            sql(seed, database=True)
            print('Dummy data imported into local MySQL.')
        target = ROOT / 'backend/uploads/dummy'
        target.mkdir(parents=True, exist_ok=True)
        for asset in (ROOT / 'frontend/dev-data/assets').iterdir():
            if asset.is_file() and not (target / asset.name).exists():
                shutil.copy2(asset, target / asset.name)
        print('Local media ready.')
