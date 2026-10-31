"""Portable local checks; does not require Docker or any application secrets."""
import argparse
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--browser', action='store_true')
    args = parser.parse_args()
    npm = 'npm.cmd' if os.name == 'nt' else 'npm'
    gradle = ROOT / 'backend' / ('gradlew.bat' if os.name == 'nt' else 'gradlew')
    subprocess.run([str(gradle), 'test', 'jacocoTestReport', '--no-daemon'], cwd=ROOT / 'backend', check=True)
    subprocess.run([sys.executable, 'scripts/check-test-coverage.py', 'backend'], cwd=ROOT, check=True)
    subprocess.run([sys.executable, '-m', 'unittest', 'discover', '-s', 'scripts/tests'], cwd=ROOT, check=True)
    for script in ['test:coverage', 'build', *(['test:e2e', 'test:preview', 'test:visual'] if args.browser else [])]:
        subprocess.run([npm, 'run', script], cwd=ROOT / 'frontend', check=True)
    subprocess.run([sys.executable, 'scripts/check-test-coverage.py', 'frontend'], cwd=ROOT, check=True)


if __name__ == '__main__':
    main()
