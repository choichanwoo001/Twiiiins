"""Read-only deployment report. Never subscribes an address or sends a message."""
import argparse
import json
import os
from pathlib import Path
import time
from urllib.request import Request, urlopen


def read_available(site):
    request = Request(site.rstrip('/') + '/api/newsletter/status',
                      headers={'User-Agent': 'TWIIIINS-deployment-check', 'Cache-Control': 'no-cache'})
    with urlopen(request, timeout=10) as response:
        result = json.load(response)
    available = result.get('data', {}).get('available')
    if result.get('success') is not True or type(available) is not bool:
        raise ValueError('Invalid newsletter status response')
    return available


def report(available, expect_ready=False):
    summary = '## Deployment result\n\n- Website: deployed; site health check passed.\n'
    if available:
        summary += '- Newsletter: required configuration is ready.\n- SMTP delivery: not tested by deployment.\n'
        code = 0
    else:
        summary += '- Newsletter: not ready; subscriptions and sending remain disabled.\n- SMTP delivery: not tested by deployment.\n'
        level = 'error' if expect_ready else 'warning'
        print(f'::{level}::Website deployed, but newsletter is not ready. Check administrator newsletter settings.')
        code = 1 if expect_ready else 0
    print(summary)
    if os.environ.get('GITHUB_STEP_SUMMARY'):
        with Path(os.environ['GITHUB_STEP_SUMMARY']).open('a', encoding='utf-8') as output:
            output.write(summary + '\n')
    return code


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('site')
    parser.add_argument('--expect-ready', action='store_true')
    args = parser.parse_args()
    for attempt in range(3):
        try:
            return report(read_available(args.site), args.expect_ready)
        except Exception:
            if attempt < 2:
                time.sleep(2)
    print('::error::Website health passed, but newsletter readiness could not be verified. No subscription or email was sent.')
    return 1


if __name__ == '__main__':
    raise SystemExit(main())
