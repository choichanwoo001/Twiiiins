import { test as base, expect } from '@playwright/test'

export const test = base.extend({
  page: async ({ page }, use) => {
    const errors = []
    page.on('pageerror', error => errors.push(error.message))
    await use(page)
    expect(errors, 'unexpected browser exceptions').toEqual([])
  },
})
export { expect }

// Only known empty GET resources may use a default response. Writes must be explicit.
const emptyReads = new Set(['/api/concerts', '/api/concerts/upcoming', '/api/concerts/past',
  '/api/projects', '/api/media/photo-groups', '/api/media/music', '/api/media/videos',
  '/api/media/news', '/api/media/equipment', '/api/media/contacts', '/api/media/download-files'])
export function emptyApiResponse(request) {
  const path = new URL(request.url()).pathname
  if (request.method() === 'GET' && path === '/api/admin/translations') return { available: false, items: [] }
  if (request.method() === 'GET' && emptyReads.has(path)) return []
  throw new Error(`Missing API mock: ${request.method()} ${path}`)
}

export async function publicApi(page, overrides = {}) {
  await page.addInitScript(() => {
    if (window !== window.top) return
    if (!localStorage.getItem('twiiiins-cookie-preferences')) localStorage.setItem('twiiiins-cookie-preferences', JSON.stringify({ version: 1, media: false, savedAt: Date.now() }))
  })
  await page.route('**/api/**', async route => {
    const request = route.request(), path = new URL(request.url()).pathname
    if (!path.startsWith('/api/')) return route.continue()
    if (Object.hasOwn(overrides, path)) return route.fulfill({ json: { success: true, data: overrides[path] } })
    if (path === '/api/newsletter/status') return route.fulfill({ json: { success: true, data: { available: false } } })
    return route.fulfill({ json: { success: true, data: emptyApiResponse(request) } })
  })
}
