"""Local Gemini wire-format fixture. No external calls, no real credentials."""
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        try:
            if not self.path.startswith('/v1beta/models/') or not self.path.endswith(':generateContent'):
                self.send_error(404)
                return
            if self.headers.get('x-goog-api-key') != 'local-stub-key':
                self.send_error(401)
                return
            request = json.loads(self.rfile.read(int(self.headers['Content-Length'])))
            parts = request['contents'][0]['parts']
            # The application sends its instruction and source in separate parts.
            source = parts[-1]['text']
            if request['generationConfig']['responseMimeType'] != 'application/json':
                raise ValueError('Expected structured output')
            try:
                pair = json.loads(source)
                if isinstance(pair, list):
                    source = next((v for v in pair if v), source)
            except (ValueError, TypeError):
                pass
            result = {'sourceLanguage': 'en', 'en': source, 'de': source.replace('Hello', 'Hallo').replace('Project', 'Projekt')}
            response = {'candidates': [{'finishReason': 'STOP', 'content': {'parts': [{'text': json.dumps(result)}]}}]}
            body = json.dumps(response).encode()
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        except (KeyError, ValueError, TypeError):
            self.send_error(400)


if __name__ == '__main__':
    ThreadingHTTPServer(('0.0.0.0', 8090), Handler).serve_forever()
