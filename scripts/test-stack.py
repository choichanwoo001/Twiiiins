"""Owns only the twiiiins-test Compose project; never uses production Compose/.env."""
import argparse
import os
from pathlib import Path
import subprocess
import sys
import time
from urllib.request import urlopen
import uuid

ROOT = Path(__file__).resolve().parents[1]
COMPOSE = ['docker', 'compose', '--env-file', str(ROOT / 'tests/stack.env'),
           '-p', 'twiiiins-test', '-f', str(ROOT / 'tests/compose.yml')]


def compose(*args, **kwargs):
    return subprocess.run([*COMPOSE, *args], cwd=ROOT, check=True, **kwargs)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['up', 'down', 'run', 'logs', 'restart'])
    parser.add_argument('--repeat', type=int, default=1)
    parser.add_argument('--linux-browsers', action='store_true', help='Run browsers in the pinned Linux image, with localhost forwarding')
    args = parser.parse_args()
    if args.repeat < 1:
        parser.error('--repeat must be positive')
    if args.action == 'up':
        compose('up', '-d', '--build', '--wait', '--wait-timeout', '240')
    elif args.action == 'down':
        compose('down', '-v', '--remove-orphans')
    elif args.action == 'logs':
        compose('logs', '--no-color')
    elif args.action == 'restart':
        compose('restart', 'backend')
    else:
        browser_name = None
        try:
            if args.linux_browsers:
                browser_name = 'twiiiins-test-browsers-' + uuid.uuid4().hex[:8]
                subprocess.run(['docker', 'run', '-d', '--rm', '--init', '--ipc=host',
                    '--name', browser_name, '-p', '127.0.0.1:18081:3000',
                    '--mount', f'type=bind,source={ROOT / "frontend/node_modules"},target=/client/node_modules,readonly',
                    'mcr.microsoft.com/playwright:v1.63.0-noble', 'node',
                    '/client/node_modules/playwright/cli.js', 'run-server', '--port', '3000', '--host', '0.0.0.0'], check=True)
                deadline = time.monotonic() + 60
                while True:
                    try:
                        with urlopen('http://127.0.0.1:18081', timeout=2) as response:
                            if response.status == 200:
                                break
                    except OSError:
                        if time.monotonic() >= deadline:
                            raise RuntimeError('Linux browser server did not become ready')
                        time.sleep(0.25)
            for _ in range(args.repeat):
                # Fresh DB, mailbox and uploads for every repeat.
                compose('down', '-v', '--remove-orphans')
                compose('up', '-d', '--build', '--wait', '--wait-timeout', '240')
                env = {**os.environ, 'TWIIIINS_TEST_STACK': '1'}
                if args.linux_browsers:
                    env['TWIIIINS_BROWSER_WS_ENDPOINT'] = 'ws://127.0.0.1:18081/'
                subprocess.run(['npm.cmd' if os.name == 'nt' else 'npm', 'run', 'test:integration'],
                               cwd=ROOT / 'frontend', env=env, check=True)
        finally:
            output = ROOT / '.local/test-artifacts'
            output.mkdir(parents=True, exist_ok=True)
            with (output / 'stack.log').open('w', encoding='utf-8') as log:
                subprocess.run([*COMPOSE, 'logs', '--no-color'], stdout=log, stderr=subprocess.STDOUT, cwd=ROOT)
            try:
                compose('down', '-v', '--remove-orphans')
            finally:
                if browser_name:
                    with (output / 'browser-server.log').open('w', encoding='utf-8') as log:
                        subprocess.run(['docker', 'logs', browser_name], stdout=log, stderr=subprocess.STDOUT)
                    subprocess.run(['docker', 'stop', browser_name], check=True)


if __name__ == '__main__':
    main()
