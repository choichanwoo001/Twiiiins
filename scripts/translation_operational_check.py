"""Verify real translation using private temporary drafts; never publishes or sends mail."""
import hashlib
import importlib.util
import json
from pathlib import Path
import secrets
import time
import socket
import subprocess
import sys
import urllib.request
import urllib.error
from datetime import date


def diagnose():
    spec = importlib.util.spec_from_file_location('newsletter_operations', Path(__file__).with_name('newsletter-operational-check.py'))
    operations = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(operations)
    env = operations.environment()
    key = env.get('GEMINI_API_KEY', '')
    print(json.dumps({'keyConfigured': bool(key), 'enabled': env.get('TRANSLATION_ENABLED'),
        'model': env.get('GEMINI_MODEL'), 'timeout': env.get('TRANSLATION_TIMEOUT_SECONDS'),
        'containerProxyConfigured': any(env.get(k) for k in ('HTTPS_PROXY', 'HTTP_PROXY', 'https_proxy', 'http_proxy'))}), flush=True)
    host = 'generativelanguage.googleapis.com'
    try:
        addresses = socket.getaddrinfo(host, 443, type=socket.SOCK_STREAM)
        print(json.dumps({'dnsIPv4': sum(a[0] == socket.AF_INET for a in addresses),
            'dnsIPv6': sum(a[0] == socket.AF_INET6 for a in addresses)}), flush=True)
    except Exception as error:
        print('DNS failed: ' + type(error).__name__, flush=True)
    url = 'https://' + host + '/v1beta/models'
    try:
        with urllib.request.urlopen(urllib.request.Request(url, headers={'x-goog-api-key': key}), timeout=15) as response:
            models = json.load(response).get('models', [])
            print(json.dumps({'hostProviderHttp': response.status, 'flashModels': [m['name'] for m in models if 'flash' in m.get('name', '')]}), flush=True)
    except urllib.error.HTTPError as error:
        print(json.dumps({'hostProviderHttp': error.code, 'message': error.read().decode().replace(key, '[REDACTED]')[:1500]}), flush=True)
    except Exception as error:
        print('Host provider connection failed: ' + type(error).__name__, flush=True)
    for family in ('--ipv4', '--ipv6'):
        result = subprocess.run(['docker', 'compose', 'exec', '-T', 'backend', 'curl', family, '--http1.1',
            '--silent', '--show-error', '--connect-timeout', '8', '--max-time', '15', '--output', '/tmp/gemini-model-check.json',
            '--write-out', '%{http_code}', '--header', '@-', url], input='x-goog-api-key: ' + key + '\n',
            text=True, capture_output=True, timeout=25)
        print(json.dumps({'containerFamily': family, 'exitCode': result.returncode,
            'httpStatus': result.stdout[-10:], 'error': result.stderr.replace(key, '[REDACTED]')[:300]}), flush=True)
    generation_url = 'https://' + host + '/v1beta/models/' + env.get('GEMINI_MODEL', 'gemini-3.8-flash') + ':generateContent'
    body = json.dumps({'contents': [{'parts': [{'text': 'Translate Hello TWIIIINS to German.'}]}]})
    try:
        with urllib.request.urlopen(urllib.request.Request(generation_url, data=body.encode(),
            headers={'x-goog-api-key': key, 'Content-Type': 'application/json'}), timeout=35) as response:
            data = json.load(response)
            print(json.dumps({'hostGenerateHttp': response.status,
                'finishReason': data.get('candidates', [{}])[0].get('finishReason')}), flush=True)
    except urllib.error.HTTPError as error:
        print(json.dumps({'hostGenerateHttp': error.code, 'message': error.read().decode().replace(key, '[REDACTED]')[:1500]}), flush=True)
    except Exception as error:
        print('Host generation failed: ' + type(error).__name__, flush=True)
    config = 'header = ' + json.dumps('x-goog-api-key: ' + key) + '\nheader = "Content-Type: application/json"\ndata = ' + json.dumps(body) + '\n'
    result = subprocess.run(['docker', 'compose', 'exec', '-T', 'backend', 'curl', '--ipv4', '--http1.1',
        '--silent', '--show-error', '--connect-timeout', '8', '--max-time', '35', '--config', '-',
        '--write-out', '\n%{http_code}', generation_url], input=config, text=True, capture_output=True, timeout=45)
    response_body, _, code = result.stdout.rpartition('\n')
    print(json.dumps({'containerGenerateHttp': code, 'exitCode': result.returncode,
        'error': result.stderr.replace(key, '[REDACTED]')[:300],
        'providerError': response_body.replace(key, '[REDACTED]')[:1500] if code != '200' else None}), flush=True)


def verify():
    spec = importlib.util.spec_from_file_location('newsletter_operations', Path(__file__).with_name('newsletter-operational-check.py'))
    operations = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(operations)
    token = secrets.token_urlsafe(32)
    digest = hashlib.sha256(token.encode()).hexdigest()
    drafts = []
    operations.sql("INSERT INTO admin_sessions(token_hash,username,expires_at) VALUES ('" + digest
        + "','translation-operational-check',DATE_ADD(UTC_TIMESTAMP(),INTERVAL 15 MINUTE));")
    try:
        status = operations.request('/admin/translations', token=token)
        if not status.get('available'):
            raise RuntimeError('Translation settings are unavailable')
        for language, title, body in [
            ('en', 'A welcome from TWIIIINS', '<p>Welcome to our next concert. TWIIIINS looks forward to seeing you.</p>'),
            ('de', 'Willkommen bei TWIIIINS', '<p>Wir freuen uns auf das nächste Konzert mit TWIIIINS.</p>')
        ]:
            payload = {'date': date.today().isoformat(), 'title' if language == 'en' else 'titleDe': title,
                'bodyEn' if language == 'en' else 'bodyDe': body, 'imageUrls': [], 'videoUrls': []}
            draft = operations.request('/admin/news', payload, token)
            draft_id = int(draft['id'])
            drafts.append(draft_id)
            deadline = time.monotonic() + 150
            while True:
                result = operations.request(f'/admin/translations?kind=news&contentId={draft_id}', token=token)
                rows = {row['field']: row for row in result['items']}
                required = [rows.get('title'), rows.get('bodyEn')]
                if all(row and row['state'] == 'COMPLETED' for row in required):
                    break
                errors = [row.get('errorCode') for row in required if row and row.get('errorCode')]
                if any(row and row['state'] == 'FAILED' for row in required) or time.monotonic() >= deadline:
                    raise RuntimeError('Real translation failed: ' + ','.join(errors or ['completion timeout']))
                time.sleep(3)
            saved = operations.request(f'/admin/news/{draft_id}', token=token)
            if saved['status'] != 'DRAFT' or not all(saved.get(field) for field in ('title', 'titleDe', 'bodyEn', 'bodyDe')):
                raise RuntimeError('Translated native fields were not persisted')
            if saved['title' if language == 'en' else 'titleDe'] != title or saved['bodyEn' if language == 'en' else 'bodyDe'] != body:
                raise RuntimeError('Authored source changed during translation')
            if any('TWIIIINS' not in saved[field] for field in ('title', 'titleDe', 'bodyEn', 'bodyDe')):
                raise RuntimeError('Proper name was not preserved')
            preview = operations.request(f'/admin/news/{draft_id}/preview?language={"de" if language == "en" else "en"}', token=token)
            if saved['titleDe' if language == 'en' else 'title'] != preview['title']:
                raise RuntimeError('Preview did not use translated content')
            if any(n['id'] == draft_id for n in operations.request('/media/news')):
                raise RuntimeError('Private draft unexpectedly appeared in public content')
            operations.request(f'/admin/news/{draft_id}', token=token, method='DELETE')
            drafts.remove(draft_id)
            print(json.dumps({'sourceLanguage': language, 'realGeminiTranslation': 'PASS', 'nativeFields': 'PASS',
                'preview': 'PASS', 'sourcePreserved': True, 'privateDraftDeleted': True, 'emailsRequested': 0}), flush=True)
    finally:
        try:
            for draft_id in drafts:
                operations.request(f'/admin/news/{draft_id}', token=token, method='DELETE')
        finally:
            operations.sql("DELETE FROM admin_sessions WHERE token_hash='" + digest + "';")


if __name__ == '__main__':
    try:
        diagnose() if '--diagnose' in sys.argv else verify()
    except Exception as error:
        print('Translation operational check failed: ' + (str(error) if isinstance(error, RuntimeError) else type(error).__name__), flush=True)
        raise SystemExit(1)
