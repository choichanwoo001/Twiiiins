import importlib.util
import io
import json
import unittest
from pathlib import Path
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('operations',
    Path(__file__).resolve().parents[1] / 'newsletter-operational-check.py')
operations = importlib.util.module_from_spec(spec)
spec.loader.exec_module(operations)


class NewsletterOperationsTest(unittest.TestCase):
    def test_successful_commands_can_return_only_a_message(self):
        response = io.BytesIO(json.dumps({'success': True, 'message': 'Test queued'}).encode())
        with patch.object(operations.urllib.request, 'urlopen', return_value=response):
            self.assertIsNone(operations.request('/admin/news/1/test', {'language': 'en'}))

    def test_settings_response_keeps_data(self):
        data = {'available': True, 'configurationIssues': []}
        response = io.BytesIO(json.dumps({'success': True, 'data': data}).encode())
        with patch.object(operations.urllib.request, 'urlopen', return_value=response):
            self.assertEqual(operations.request('/admin/newsletter/settings'), data)

    def test_preflight_refuses_existing_pending_deliveries_without_mutation(self):
        with patch.object(operations, 'sql', return_value='2') as sql, \
             patch.object(operations, 'environment', return_value={}), \
             patch.object(operations, 'request', return_value={'available': False}), \
             patch('builtins.print'):
            with self.assertRaisesRegex(RuntimeError, 'Review pending deliveries'):
                operations.preflight()
            self.assertEqual(sql.call_count, 1)
            self.assertTrue(sql.call_args.args[0].startswith('SELECT'))

    def test_database_errors_do_not_disclose_values(self):
        result = type('Result', (), {'returncode': 1, 'stderr': "ERROR 1267 (HY000): sensitive recipient", 'stdout': ''})()
        with patch.object(operations.subprocess, 'run', return_value=result):
            with self.assertRaisesRegex(RuntimeError, '^Database operation failed: SELECT, MySQL 1267$'):
                operations.sql("SELECT 'sensitive recipient';")

    def test_failed_publication_cleans_up_its_test_draft_and_session(self):
        draft = {'id': 99, 'title': 'Draft'}
        updated = {**draft, 'title': '[Technical check] Saved publication'}
        with patch.object(operations, 'sql', return_value='') as sql, \
             patch.object(operations, 'request', side_effect=[draft, updated, {'status': 'DRAFT'}, None]) as request:
            with self.assertRaisesRegex(RuntimeError, 'Publication status did not persist'):
                operations.verify_publication()
            self.assertEqual(request.call_args.kwargs['method'], 'DELETE')
            self.assertEqual(request.call_args.args[0], '/admin/news/99')
            self.assertTrue(sql.call_args.args[0].startswith('DELETE FROM admin_sessions'))
            self.assertFalse(any('/send' in call.args[0] for call in request.call_args_list))

    def test_diagnosis_reports_missing_fields_without_disclosing_content(self):
        row = json.dumps({'id': 1, 'status': 'DRAFT', 'archived': 0,
            'title': 'Private title', 'titleDe': '', 'bodyEn': '<p>Private text</p>', 'bodyDe': '<p></p>'})
        result = type('Result', (), {'stdout': '', 'stderr': ''})()
        with patch.object(operations, 'sql', return_value=row) as sql, \
             patch.object(operations.subprocess, 'run', return_value=result), \
             patch('sys.stdout', new_callable=io.StringIO) as output:
            operations.diagnose()
            self.assertNotIn('Private', output.getvalue())
            report = json.loads(output.getvalue().splitlines()[0])
            self.assertEqual(report['missingPublicationFields'], ['titleDe', 'bodyDe'])
            self.assertTrue(sql.call_args.args[0].startswith('SELECT'))


if __name__ == '__main__':
    unittest.main()
