"""Explicit server-admin operation; never publishes or sends to the subscriber list.

Run in the production compose directory. Preflight is read-only. Verify creates a
short-lived admin session and a private draft, sends EN/DE tests to the first
allowlisted test recipient, and tests signup/unsubscribe only for a new address.
Credentials, recipient addresses and unsubscribe tokens are never printed.
"""
import base64
import hashlib
import hmac
import json
import re
import secrets
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import date


def sql(statement):
    result = subprocess.run(
        ['docker', 'compose', 'exec', '-T', 'db', 'sh', '-c',
         'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -u root --batch --skip-column-names "$MYSQL_DATABASE"'],
        input=statement, text=True, capture_output=True, check=False)
    if result.returncode:
        code = re.search(r'ERROR\s+(\d+)\s+\(([A-Z0-9]+)\)', result.stderr)
        # MySQL error numbers and operation names are safe; SQL values are not.
        raise RuntimeError('Database operation failed: ' + statement.split()[0]
            + ', MySQL ' + (code.group(1) if code else 'unknown'))
    return result.stdout.strip()


def quote(value):
    # Binary literals avoid collation conflicts between legacy tables and the
    # mysql client's default connection collation. Addresses are normalized.
    return "X'" + value.encode().hex() + "'"


def environment():
    result = subprocess.run(['docker', 'inspect', 'twiiiins-backend'],
                            text=True, capture_output=True, check=True)
    return dict(item.split('=', 1) for item in json.loads(result.stdout)[0]['Config']['Env'] if '=' in item)


def request(path, body=None, token=None, method=None):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request('http://127.0.0.1:8080/api' + path,
        data=json.dumps(body).encode() if body is not None else None,
        headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as response:
            result = json.load(response)
        if not result.get('success'):
            raise RuntimeError('API returned failure (details withheld)')
        # Successful command endpoints return a message without a data field.
        return result.get('data')
    except urllib.error.HTTPError as error:
        raise RuntimeError('API request failed, HTTP ' + str(error.code)) from None


def preflight():
    pending = int(sql("SELECT COUNT(*) FROM newsletter_deliveries WHERE status IN ('WAITING','SENDING');"))
    env = environment()
    print(json.dumps({'pendingDeliveries': pending,
        'existingTokenKeyPresent': bool(env.get('NEWSLETTER_TOKEN_KEY')),
        'currentlyAvailable': request('/newsletter/status')['available']}), flush=True)
    if pending:
        raise RuntimeError('Review pending deliveries before enabling mail; nothing was changed')


def wait_delivery(where):
    for _ in range(90):
        rows = sql('SELECT status,COALESCE(error_code,\'\') FROM newsletter_deliveries WHERE ' + where + ' ORDER BY id DESC LIMIT 1;')
        status = rows.split('\t')[0] if rows else ''
        if status == 'ACCEPTED':
            return
        if status in ('FAILED', 'UNKNOWN', 'SKIPPED'):
            raise RuntimeError('Test delivery failed: ' + rows)
        time.sleep(2)
    raise RuntimeError('Test delivery was not accepted within the verification window')


def unsubscribe(subscriber_id, key):
    version = sql('SELECT unsubscribe_version FROM newsletter_subscribers WHERE id=' + str(subscriber_id) + ';')
    context = f'unsubscribe|{subscriber_id}|{version}'
    encoded = base64.urlsafe_b64encode(context.encode()).rstrip(b'=')
    signature = base64.urlsafe_b64encode(hmac.new(key.encode(), encoded, hashlib.sha256).digest()).rstrip(b'=')
    request('/newsletter/unsubscribe', {'token': (encoded + b'.' + signature).decode()})


def verify():
    env = environment()
    recipients = [x.strip() for x in env.get('NEWSLETTER_TEST_RECIPIENTS', '').split(',') if x.strip()]
    if not recipients or not request('/newsletter/status')['available']:
        raise RuntimeError('Newsletter is not ready or test recipients are missing')
    email = recipients[0].lower()
    # All sends use the test API or this exact allowlisted signup address.
    token = secrets.token_urlsafe(32)
    digest = hashlib.sha256(token.encode()).hexdigest()
    draft_id = subscriber_id = None
    old = sql('SELECT id FROM newsletter_subscribers WHERE email=' + quote(email) + ';')
    sql("INSERT INTO admin_sessions(token_hash,username,expires_at) VALUES ('" + digest
        + "','newsletter-operational-check',DATE_ADD(UTC_TIMESTAMP(),INTERVAL 15 MINUTE));")
    try:
        settings = request('/admin/newsletter/settings', token=token)
        if settings['configurationIssues'] or settings['paused']:
            raise RuntimeError('Newsletter configuration is incomplete or mail is paused')
        draft = request('/admin/news', {'date': date.today().isoformat(),
            'title': 'TWIIIINS newsletter delivery check',
            'titleDe': 'TWIIIINS Newsletter-Versandtest',
            'bodyEn': '<p>This is a technical delivery test for the TWIIIINS newsletter. No newsletter was sent to the subscriber list.</p>',
            'bodyDe': '<p>Dies ist ein technischer Versandtest des TWIIIINS-Newsletters. Es wurde kein Newsletter an die Abonnentenliste versendet.</p>',
            'imageUrls': [], 'videoUrls': []}, token)
        draft_id = int(draft['id'])
        baseline = int(sql('SELECT COALESCE(MAX(id),0) FROM newsletter_deliveries;'))
        for language in ('en', 'de'):
            preview = request(f'/admin/news/{draft_id}/preview?language={language}', token=token)
            if not preview['html'] or not preview['title']:
                raise RuntimeError('Empty preview')
            request(f'/admin/news/{draft_id}/test', {'language': language, 'email': email}, token)
            wait_delivery("kind='TEST' AND id>" + str(baseline) + ' AND email=' + quote(email) + ' AND language=' + quote(language))
            print('Newsletter ' + language + ': SMTP accepted', flush=True)
        if not old:
            request('/newsletter/subscribe', {'email': email, 'language': 'en', 'consent': True})
            row = sql('SELECT id,status FROM newsletter_subscribers WHERE email=' + quote(email) + ';').split('\t')
            subscriber_id = int(row[0])
            if row[1] != 'ACTIVE':
                raise RuntimeError('Test signup did not activate')
            wait_delivery("kind='WELCOME' AND subscriber_id=" + str(subscriber_id))
            request('/newsletter/subscribe', {'email': email, 'language': 'de', 'consent': True})
            if sql('SELECT language FROM newsletter_subscribers WHERE id=' + str(subscriber_id) + ';') != 'de':
                raise RuntimeError('Language update failed')
            unsubscribe(subscriber_id, env['NEWSLETTER_TOKEN_KEY'])
            if sql('SELECT status FROM newsletter_subscribers WHERE id=' + str(subscriber_id) + ';') != 'UNSUBSCRIBED':
                raise RuntimeError('Test unsubscribe failed')
            print('Signup, welcome SMTP, language update and unsubscribe: PASS; test address unsubscribed', flush=True)
        else:
            print('Existing test-recipient subscription preserved; signup/unsubscribe mutation skipped', flush=True)
        print('Inbox receipt still requires recipient confirmation; no subscriber-list send was requested', flush=True)
    finally:
        try:
            if subscriber_id is not None:
                unsubscribe(subscriber_id, env['NEWSLETTER_TOKEN_KEY'])
            if draft_id is not None:
                request('/admin/news/' + str(draft_id), token=token, method='DELETE')
        finally:
            sql("DELETE FROM admin_sessions WHERE token_hash='" + digest + "';")


if __name__ == '__main__':
    try:
        operation = sys.argv[1] if len(sys.argv) == 2 else ''
        if operation == 'preflight':
            preflight()
        elif operation == 'verify':
            verify()
        else:
            raise RuntimeError('Choose preflight or verify')
    except Exception as error:
        # Avoid printing request bodies, SMTP configuration or exception tracebacks.
        print('Operational check failed: ' + (str(error) if isinstance(error, RuntimeError) else type(error).__name__), flush=True)
        sys.exit(1)
