"""Repeat the required suite with fresh DBs, no retries, and per-round evidence."""
import argparse
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time
from threading import RLock
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def xml_counts(path):
    root = ET.parse(path).getroot()
    suites = [root] if root.tag == 'testsuite' else root.findall('testsuite')
    counts = {key: sum(int(s.get(key, 0)) for s in suites)
              for key in ['tests', 'failures', 'errors', 'skipped']}
    assert counts['tests'] > 0 and not any(counts[k] for k in ['failures', 'errors', 'skipped']), (path, counts)
    return counts


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--repeat', type=int, default=3)
    args = parser.parse_args()
    if args.repeat < 1:
        parser.error('--repeat must be positive')
    npm = 'npm.cmd' if os.name == 'nt' else 'npm'
    gradle = str(ROOT / 'backend' / ('gradlew.bat' if os.name == 'nt' else 'gradlew'))
    stamp = datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ')
    output = ROOT / '.local/test-artifacts' / ('repeat-' + stamp)
    output.mkdir(parents=True)
    summary = {'started_at': stamp, 'requested': args.repeat, 'completed': 0,
               'status': 'running', 'rounds': []}
    env = {**os.environ, 'CI': 'true', 'PYTHONUTF8': '1', 'COMPOSE_PROGRESS': 'plain', 'TEST_SUITE': 'weekly'}
    evidence_lock = RLock()

    def save():
        with evidence_lock:
            (output / 'summary.json').write_text(json.dumps(summary, indent=2) + '\n', encoding='utf-8')

    def run(name, command, folder, round_dir, row):
        print(f'[{row["number"]}/{args.repeat}] {name} started', flush=True)
        started = time.monotonic()
        log_path = round_dir / (name + '.log')
        with log_path.open('w', encoding='utf-8') as log:
            result = subprocess.run(command, cwd=ROOT / folder, env=env, stdout=log, stderr=subprocess.STDOUT)
        with evidence_lock:
            row['steps'].append({'name': name, 'exit_code': result.returncode,
                                 'seconds': round(time.monotonic() - started, 2)})
            save()
        if result.returncode:
            raise RuntimeError(f'{name} failed; see {log_path}')
        print(f'[{row["number"]}/{args.repeat}] {name} passed', flush=True)

    def frontend_report(name, path, round_dir, row):
        target = round_dir / (name + '.xml')
        shutil.copy2(ROOT / 'frontend' / path, target)
        with evidence_lock:
            row['counts'][name] = xml_counts(target)
            save()

    def backend_checks(round_dir, row):
        run('backend', [gradle, 'test', 'mysqlTest', 'bootJar', '--rerun-tasks', '--no-daemon'], 'backend', round_dir, row)
        for task in ['test', 'mysqlTest']:
            reports = list((ROOT / 'backend/build/test-results' / task).glob('TEST-*.xml'))
            assert reports, f'No reports for {task}'
            target = round_dir / task
            target.mkdir()
            totals = {'tests': 0, 'failures': 0, 'errors': 0, 'skipped': 0}
            for report in reports:
                shutil.copy2(report, target / report.name)
                for key, value in xml_counts(report).items():
                    totals[key] += value
            with evidence_lock:
                row['counts'][task] = totals
                save()
        run('backend-coverage', [sys.executable, 'scripts/check-test-coverage.py', 'backend'], '.', round_dir, row)

    def frontend_checks(round_dir, row):
        run('unit', [npm, 'run', 'test:coverage', '--', '--reporter=default', '--reporter=junit', '--outputFile=.cache/unit.xml'], 'frontend', round_dir, row)
        frontend_report('unit', '.cache/unit.xml', round_dir, row)
        run('frontend-build', [npm, 'run', 'build'], 'frontend', round_dir, row)
        run('browser', [npm, 'run', 'test:e2e'], 'frontend', round_dir, row)
        frontend_report('browser', '.cache/playwright.xml', round_dir, row)
        run('preview', [npm, 'run', 'test:preview'], 'frontend', round_dir, row)
        frontend_report('preview', '.cache/playwright.xml', round_dir, row)
        run('frontend-coverage', [sys.executable, 'scripts/check-test-coverage.py', 'frontend'], '.', round_dir, row)

    try:
        save()
        for number in range(1, args.repeat + 1):
            round_dir = output / f'round-{number:02d}'
            round_dir.mkdir()
            row = {'number': number, 'steps': [], 'counts': {}}
            summary['rounds'].append(row)
            run('python', [sys.executable, '-m', 'unittest', 'discover', '-s', 'scripts/tests'], '.', round_dir, row)
            # Separate databases and output trees let the two independent groups run together.
            # Migrations and real E2E still run serially because they share one Compose project.
            with ThreadPoolExecutor(max_workers=2) as pool:
                checks = [pool.submit(backend_checks, round_dir, row), pool.submit(frontend_checks, round_dir, row)]
                for check in checks:
                    check.result()
            run('migrations', [sys.executable, 'scripts/test-migrations.py'], '.', round_dir, row)
            run('integration', [sys.executable, 'scripts/test-stack.py', 'run'], '.', round_dir, row)
            frontend_report('integration', '.cache/integration.xml', round_dir, row)
            shutil.copy2(ROOT / '.local/test-artifacts/stack.log', round_dir / 'stack.log')
            summary['completed'] = number
            save()
        summary['status'] = 'passed'
    except BaseException as error:
        summary['status'] = 'failed'
        summary['error'] = str(error)
        raise
    finally:
        summary['finished_at'] = datetime.now(timezone.utc).isoformat()
        save()
        print(f'Reliability evidence: {output / "summary.json"}', flush=True)


if __name__ == '__main__':
    main()
