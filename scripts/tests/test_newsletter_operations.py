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


if __name__ == '__main__':
    unittest.main()
