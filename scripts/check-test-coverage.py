"""Fail if measured coverage falls below the checked-in baseline (no auto-update)."""
import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def measure(kind):
    if kind == 'frontend':
        summary = json.loads((ROOT / 'frontend/coverage/coverage-summary.json').read_text())['total']
        return {key: summary[key]['pct'] for key in ('lines', 'branches')}
    tree = ET.parse(ROOT / 'backend/build/reports/jacoco/test/jacocoTestReport.xml')
    result = {}
    for key, counter in [('lines', 'LINE'), ('branches', 'BRANCH')]:
        row = tree.getroot().find(f"counter[@type='{counter}']")
        covered, missed = int(row.get('covered')), int(row.get('missed'))
        result[key] = 100 * covered / max(1, covered + missed)
    return result


def critical_backend():
    tree = ET.parse(ROOT / 'backend/build/reports/jacoco/test/jacocoTestReport.xml')
    prefixes = ('com/twiiiins/validation/', 'com/twiiiins/security/Tokens',
                'com/twiiiins/security/AdminTokenFilter', 'com/twiiiins/newsletter/RequestThrottle')
    classes = [c for p in tree.getroot().findall('package') for c in p.findall('class')
               if c.get('name').startswith(prefixes)]
    for kind, minimum in [('LINE', 80), ('BRANCH', 70)]:
        rows = [c.find(f"counter[@type='{kind}']") for c in classes]
        covered = sum(int(r.get('covered')) for r in rows if r is not None)
        missed = sum(int(r.get('missed')) for r in rows if r is not None)
        percentage = 100 * covered / max(1, covered + missed)
        assert percentage >= minimum, f'Critical backend {kind}: {percentage:.2f}% < {minimum}%'


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('kind', choices=['backend', 'frontend'])
    args = parser.parse_args()
    actual = measure(args.kind)
    baseline = json.loads((ROOT / 'tests/coverage-baseline.json').read_text())[args.kind]
    for metric, floor in baseline.items():
        assert actual[metric] >= floor, f'{args.kind} {metric}: {actual[metric]:.2f}% < baseline {floor}%'
    print(f'{args.kind}: {actual}')
    if args.kind == 'backend':
        critical_backend()
