import contextlib
import importlib.util
import io
import json
import os
from pathlib import Path
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('readiness', Path(__file__).resolve().parents[1] / 'check-newsletter-readiness.py')
readiness = importlib.util.module_from_spec(spec)
spec.loader.exec_module(readiness)


class ReadinessTest(unittest.TestCase):
    def test_site_success_and_newsletter_readiness_are_separate(self):
        for available, expected, code in [(False, False, 0), (False, True, 1), (True, True, 0)]:
            with self.subTest(available=available, expected=expected), patch.dict(os.environ, {}, clear=True), contextlib.redirect_stdout(io.StringIO()) as out:
                self.assertEqual(code, readiness.report(available, expected))
                self.assertIn('Website: deployed', out.getvalue())
                self.assertIn('not tested', out.getvalue())
                if not available:
                    self.assertIn('not ready', out.getvalue())

    def test_readiness_requires_a_boolean_in_a_success_response(self):
        for payload in [{'success': True, 'data': {'available': False}}, {'success': True, 'data': {'available': 'false'}}, {'success': False, 'data': {'available': True}}]:
            with self.subTest(payload=payload), patch.object(readiness, 'urlopen', return_value=io.StringIO(json.dumps(payload))):
                if payload == {'success': True, 'data': {'available': False}}:
                    self.assertFalse(readiness.read_available('https://example.com'))
                else:
                    with self.assertRaises(ValueError):
                        readiness.read_available('https://example.com')


if __name__ == '__main__':
    unittest.main()
