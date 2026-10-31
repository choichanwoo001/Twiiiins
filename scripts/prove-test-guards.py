"""Prove selected regressions are detected; always restore the exact source bytes.

Run in an otherwise idle checkout. Expected failures are saved separately from
normal reports. Run the green suite afterwards to rebuild restored sources.
"""
import os
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ARTIFACTS = ROOT / '.local/test-artifacts/guard-proofs'
NPM = 'npm.cmd' if os.name == 'nt' else 'npm'
GRADLE = str(ROOT / 'backend' / ('gradlew.bat' if os.name == 'nt' else 'gradlew'))


def prove(name, path, original, mutation, command, cwd, evidence):
    path = ROOT / path
    before = path.read_bytes()
    source = before.decode('utf-8')
    assert source.count(original) == 1, f'{name}: expected exactly one mutation point'
    try:
        path.write_bytes(source.replace(original, mutation, 1).encode('utf-8'))
        result = subprocess.run(command, cwd=ROOT / cwd, capture_output=True, text=True,
                                encoding='utf-8', errors='replace')
        output = result.stdout + result.stderr
        ARTIFACTS.mkdir(parents=True, exist_ok=True)
        (ARTIFACTS / f'{name}.log').write_text(output, encoding='utf-8')
        assert result.returncode != 0, f'{name}: regression escaped the tests'
        assert evidence(output), f'{name}: failed for an unexpected reason; inspect proof log'
        print(f'PROVED: {name}', flush=True)
    finally:
        path.write_bytes(before)


def auth_evidence(_):
    report = ROOT / 'backend/build/test-results/test/TEST-com.twiiiins.flows.CmsFlowTest.xml'
    failures = ET.parse(report).getroot().findall('.//failure')
    return any('Status expected:<401> but was:' in f.get('message', '') for f in failures)


if __name__ == '__main__':
    prove('duplicate-submit', 'frontend/src/views/Login.vue',
          'if (isLoading.value) return', '// intentionally removed by guard proof',
          [NPM, 'exec', '--', 'vitest', 'run', 'tests/unit/login-form.test.js', '-t', 'only sends one request'],
          'frontend', lambda output: 'to be called 1 times, but got 2 times' in output)
    prove('stale-cache', 'frontend/src/services/concertService.js',
          "apiCache.deletePattern('^/concerts')", '// intentionally removed by guard proof',
          [NPM, 'exec', '--', 'vitest', 'run', 'tests/unit/services.test.js', '-t', 'invalidates the cached list'],
          'frontend', lambda output: 'to be called 2 times, but got 1 times' in output)
    prove('missing-write-authentication', 'backend/src/main/java/com/twiiiins/config/SecurityConfig.java',
          '.requestMatchers("/api/**").hasRole("ADMIN")', '.requestMatchers("/api/**").permitAll()',
          [GRADLE, 'test', '--tests', 'com.twiiiins.flows.CmsFlowTest.everyMappedProtectedWriteRejectsAnonymousAndForgedTokens', '--no-daemon'],
          'backend', auth_evidence)
    print('All representative regressions were detected. Sources restored; rerun the green suite.')
