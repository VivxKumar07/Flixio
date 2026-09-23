import urllib.request

paths = [
    '/image?url=https://image.tmdb.org/t/p/w500/1E5baAaEse26fej7uHcjOgEE2t2.jpg',
    '/image?path=/1E5baAaEse26fej7uHcjOgEE2t2.jpg',
    '/image?file=/1E5baAaEse26fej7uHcjOgEE2t2.jpg',
    '/image/w500/1E5baAaEse26fej7uHcjOgEE2t2.jpg',
    '/images/w500/1E5baAaEse26fej7uHcjOgEE2t2.jpg',
    '/img/w500/1E5baAaEse26fej7uHcjOgEE2t2.jpg',
    '/poster/w500/1E5baAaEse26fej7uHcjOgEE2t2.jpg',
    '/discover/movie?with_watch_providers=8&watch_region=US',
]

for p in paths:
    url = f'https://flixora.vivxkumar07.workers.dev{p}'
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=5) as resp:
            content = resp.read()[:60]
            print(p, '->', resp.status, resp.headers.get('Content-Type'), content)
    except Exception as e:
        print(p, '-> ERR:', e)
