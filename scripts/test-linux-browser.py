"""Run the frontend CI checks in the pinned official Linux browser image.

The source mount is read-only. Linux npm dependencies and build output live in
the disposable container; only test reports are copied to .local artifacts.
"""
import os
from pathlib import Path
import subprocess
import uuid

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / '.local/test-artifacts/linux-browser'
IMAGE = 'mcr.microsoft.com/playwright:v1.63.0-noble'
SCRIPT = r'''
set -eu
mkdir -p /work
cd /source
tar --exclude=node_modules --exclude=.cache --exclude=test-results --exclude=coverage \
    --exclude=reports --exclude=.stryker-tmp --exclude=dist --exclude='.env*' -cf - . | tar -xf - -C /work
cd /work
mkdir -p /backend/src/main/resources/newsletter
cp /templates/*.html /backend/src/main/resources/newsletter/
finish() {
  for directory in .cache test-results coverage; do
    if [ -d "$directory" ]; then cp -a "$directory" /results/; fi
  done
}
trap finish EXIT
npm ci
npm run test:coverage
npm run build
npm run test:e2e
npm run test:preview
npm run test:visual
'''


if __name__ == '__main__':
    OUTPUT.mkdir(parents=True, exist_ok=True)
    command = ['docker', 'run', '--rm', '--init', '--ipc=host',
               '--name', 'twiiiins-linux-browser-' + uuid.uuid4().hex[:8],
               '--mount', f'type=bind,source={ROOT / "frontend"},target=/source,readonly',
               '--mount', f'type=bind,source={ROOT / "backend/src/main/resources/newsletter"},target=/templates,readonly',
               '--mount', f'type=bind,source={OUTPUT},target=/results',
               '-e', 'CI=true', IMAGE, 'bash', '-c', SCRIPT]
    with (OUTPUT / 'run.log').open('w', encoding='utf-8') as log:
        result = subprocess.run(command, stdout=log, stderr=subprocess.STDOUT, cwd=ROOT)
    print(f'Linux browser checks exit={result.returncode}; reports: {OUTPUT}', flush=True)
    raise SystemExit(result.returncode)
