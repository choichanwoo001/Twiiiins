"""Portable development tools from official release archives."""
from pathlib import Path
import urllib.request
import zipfile

CACHE = Path.home() / '.cache' / 'twiiiins-local-runtime'
TOOLS = [
    ('mysql', 'https://cdn.mysql.com/archives/mysql-8.0/mysql-8.0.43-winx64.zip', 'mysql/mysql-8.0.43-winx64/bin/mysqld.exe'),
    ('mailpit', 'https://github.com/axllent/mailpit/releases/download/v1.31.4/mailpit-windows-amd64.zip', 'mailpit/mailpit.exe'),
]
if __name__ == '__main__':
    CACHE.mkdir(parents=True, exist_ok=True)
    for name, url, executable in TOOLS:
        if (CACHE / executable).exists():
            continue
        archive = CACHE / (name + '.zip')
        urllib.request.urlretrieve(url, archive)
        with zipfile.ZipFile(archive) as package:
            target = (CACHE / name).resolve()
            for entry in package.infolist():
                if not (target / entry.filename).resolve().is_relative_to(target):
                    raise RuntimeError('Unexpected archive path')
            package.extractall(target)
        print(name + ' ready', flush=True)
