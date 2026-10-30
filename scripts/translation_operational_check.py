"""Verify real translation using private temporary drafts; never publishes or sends mail."""
import hashlib
import importlib.util
import json
from pathlib import Path
import secrets
import time
from datetime import date


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
        verify()
    except Exception as error:
        print('Translation operational check failed: ' + (str(error) if isinstance(error, RuntimeError) else type(error).__name__), flush=True)
        raise SystemExit(1)
