import { createReadStream, readFileSync, statSync } from 'node:fs'
import { resolve, basename } from 'node:path'
import { fileURLToPath } from 'node:url'

const directory = fileURLToPath(new URL('.', import.meta.url))

// Development-only, read-only API. Never forwards fixture requests to production.
export function localDataPlugin() {
  const data = JSON.parse(readFileSync(resolve(directory, 'fixtures.json'), 'utf8'))
  const collections = {
    '/api/concerts': data.concerts,
    '/api/projects': data.projects,
    '/api/media/music': data['media/music'],
    '/api/media/videos': data['media/videos'],
    '/api/media/news': data['media/news'],
    '/api/media/photos/groups': data['media/photos/groups'],
    '/api/media/photo-groups': data['media/photos/groups'],
    '/api/media/equipment': data['media/equipment'],
    '/api/media/contacts': data['media/contacts'],
    '/api/media/download-files': data['media/download-files']
  }
  const photos = data['media/photos/groups'].flatMap(group => group.photos)

  return {
    name: 'twiiiins-local-data',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use((request, response, next) => {
        const url = new URL(request.url, 'http://localhost')
        const path = url.pathname.replace(/\/$/, '')
        const isAsset = path.startsWith('/uploads/dummy/')
        if (!path.startsWith('/api/') && !isAsset) return next()

        const json = (status, payload) => {
          response.statusCode = status
          response.setHeader('Content-Type', 'application/json; charset=utf-8')
          response.setHeader('Cache-Control', 'no-store')
          response.end(JSON.stringify(payload))
        }
        if (!['GET', 'HEAD'].includes(request.method)) {
          response.setHeader('Allow', 'GET, HEAD')
          return json(405, { success: false, error: { code: 'DUMMY_READ_ONLY', message: '로컬 더미 모드는 조회 전용입니다. 편집하려면 로컬 DB에 시드를 넣고 더미 모드를 끄세요.' } })
        }
        if (isAsset) {
          const name = path.slice('/uploads/dummy/'.length)
          if (!name || name !== basename(name) || !/^[a-f0-9-]+\.(jpg|pdf)$/.test(name)) {
            return json(404, { success: false })
          }
          const file = resolve(directory, 'assets', name)
          try {
            const stat = statSync(file)
            response.setHeader('Content-Type', name.endsWith('.pdf') ? 'application/pdf' : 'image/jpeg')
            response.setHeader('Content-Length', stat.size)
            if (request.method === 'HEAD') return response.end()
            const stream = createReadStream(file)
            stream.on('error', () => response.destroy())
            stream.pipe(response)
          } catch {
            json(404, { success: false })
          }
          return
        }

        let result = collections[path]
        if (path === '/api/concerts/upcoming') result = data.concerts.filter(item => !item.isPast).sort((a, b) => a.date.localeCompare(b.date))
        if (path === '/api/concerts/past') result = data.concerts.filter(item => item.isPast)
        if (path.startsWith('/api/projects/slug/')) {
          result = data.projects.find(item => item.urlSlug === path.slice('/api/projects/slug/'.length))
        }
        const photoMatch = path.match(/^\/api\/media\/photos\/(\d+)$/)
        if (photoMatch) result = photos.find(item => item.id === Number(photoMatch[1]))
        const groupPhotos = path.match(/^\/api\/media\/photos\/groups\/(\d+)\/photos$/)
        if (groupPhotos) result = data['media/photos/groups'].find(group => group.id === Number(groupPhotos[1]))?.photos
        if (result === undefined) {
          const match = path.match(/^(.*)\/(\d+)$/)
          if (match && collections[match[1]]) result = collections[match[1]].find(item => item.id === Number(match[2]))
        }
        if (result === undefined) return json(404, { success: false, error: { code: 'NOT_FOUND', message: '더미 데이터를 찾을 수 없습니다.' } })
        if (Array.isArray(result)) {
          result = result.filter(item => {
            for (const field of ['title', 'name', 'location']) {
              const query = url.searchParams.get(field)
              if (query && !String(item[field] || '').toLowerCase().includes(query.toLowerCase())) return false
            }
            const date = item.date || item.premiereDate
            if (url.searchParams.has('startDate') && (!date || date < url.searchParams.get('startDate'))) return false
            if (url.searchParams.has('endDate') && (!date || date > url.searchParams.get('endDate'))) return false
            return true
          })
        }
        return json(200, { success: true, data: result, timestamp: new Date().toISOString() })
      })
      server.config.logger.info('로컬 더미 데이터 모드: 배포 사이트 기준 스냅샷 (조회 전용)')
    }
  }
}
