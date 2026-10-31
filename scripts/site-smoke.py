"""Read-only deployment checks. Never logs response bodies or performs writes."""
import argparse
import json
from html.parser import HTMLParser
from urllib.request import urlopen
from urllib.parse import urljoin, urlsplit


class Assets(HTMLParser):
    def __init__(self):
        super().__init__()
        self.paths = set()

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        path = attrs.get('src') if tag == 'script' else attrs.get('href') if tag == 'link' and attrs.get('rel') in ('stylesheet', 'modulepreload', 'icon') else None
        if path:
            self.paths.add(path)


def check(base):
    origin = urlsplit(base)
    if origin.scheme not in ('http', 'https') or not origin.netloc:
        raise ValueError('Expected an HTTP(S) site URL')
    assets = Assets()
    for path in ['/', '/about', '/projects', '/concerts', '/media', '/shop', '/contact', '/privacy', '/login']:
        with urlopen(base.rstrip('/') + path, timeout=20) as response:
            assert response.status == 200
            body = response.read().decode('utf-8')
            assert '<div id="app"></div>' in body, path
            assets.feed(body)
    assert assets.paths, 'No build assets referenced by the site'
    for path in sorted(assets.paths):
        url = urljoin(base.rstrip('/') + '/', path)
        if urlsplit(url).netloc != origin.netloc:
            continue
        with urlopen(url, timeout=20) as response:
            assert response.status == 200 and response.read(), path
            assert response.headers.get_content_type() != 'text/html', f'Asset returned SPA fallback: {path}'
    for path in ['/api/projects', '/api/concerts', '/api/media/news', '/api/newsletter/status']:
        with urlopen(base.rstrip('/') + path, timeout=20) as response:
            data = json.load(response)
            assert data.get('success') is True and 'data' in data, path
    print('Public route, static asset and API smoke checks passed.')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('url')
    check(parser.parse_args().url)
