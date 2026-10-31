import { test, expect, emptyApiResponse } from './support/fixtures'

const translated = (en, de) => ({ state: 'COMPLETED', en, de })
const project = { id: 901, title: 'New project', subtitle: 'Collaboration with Salzburger Landestheater', urlSlug: 'translated-project', premiereDate: '2026-10-30', location: 'Vienna', descriptions: ['First paragraph', 'Second paragraph'], reviews: [{ text: 'Wonderful music', source: 'Reviewer Name' }], imageUrls: [], translations: {
  title: translated('New project', 'Neues Projekt'), subtitle: translated('Collaboration with Salzburger Landestheater', 'Eigene Übersetzung'),
  'descriptions[0]': translated('First paragraph', 'Erster Absatz'), 'descriptions[1]': translated('Second paragraph', 'Zweiter Absatz'),
  'reviews[0].text': translated('Wonderful music', 'Wunderbare Musik'), location: { state: 'FAILED' }
} }
async function mock(page, { admin = false } = {}) {
  const writes = []
  let rows = [{ id: 1, kind: 'projects', contentId: 901, field: 'title', source: 'New project', en: 'New project', de: 'Neues Projekt', state: 'COMPLETED', version: 2 },
    { id: 2, kind: 'projects', contentId: 901, field: 'subtitle', source: 'Source', en: null, de: null, state: 'FAILED', errorCode: 'GEMINI_HTTP_429', version: 1 }]
  await page.addInitScript(admin => { if (window !== window.top) return; if (admin) localStorage.setItem('token', 'test-token'); localStorage.setItem('twiiiins-cookie-preferences', JSON.stringify({ version: 1, media: false, savedAt: Date.now() })) }, admin)
  await page.route('**/api/**', async route => {
    const request = route.request(), path = new URL(request.url()).pathname
    if (!path.startsWith('/api/')) return route.continue()
    let data
    if (path === '/api/auth/me') data = { username: 'test-admin' }
    if (path === '/api/projects') data = [project]
    else if (path.startsWith('/api/projects/')) data = project
    if (path === '/api/newsletter/status') data = { available: true }
    if (path === '/api/media/videos') data = [{ id: 2, title: 'New video', embedUrl: 'https://www.youtube.com/embed/7D3tv-8Fmlw', translations: { title: translated('New video', 'Neues Video') } }]
    if (path === '/api/media/photo-groups') data = [{ id: 3, title: 'New gallery', photos: [], translations: { title: translated('New gallery', 'Neue Galerie') } }]
    if (path === '/api/media/news') data = [{ id: 4, title: 'New news', date: '2026-10-30', bodyEn: '<p>New story</p>', bodyDe: '<p>Neue Geschichte</p>', imageUrls: [], translations: { title: translated('New news', 'Neue Nachricht'), bodyEn: translated('<p>New story</p>', '<p>Neue Geschichte</p>') } }]
    if (path === '/api/admin/translations') data = { available: true, items: rows }
    if (path === '/api/admin/translations/2/retry') { writes.push({ path, body: request.postDataJSON() }); rows[1] = { ...rows[1], state: 'PENDING', errorCode: null }; data = rows[1] }
    if (path === '/api/admin/translations/1' && request.method() === 'PUT') { writes.push({ path, body: request.postDataJSON() }); rows[0] = { ...rows[0], ...request.postDataJSON(), manualDe: true, version: 3 }; data = rows[0] }
    await route.fulfill({ json: { success: true, data: data === undefined ? emptyApiResponse(route.request()) : data } })
  })
  return writes
}
async function language(page, name) { await page.locator('.website-language summary').click(); await page.getByRole('button', { name, exact: true }).click() }

test('DB variants precede static translations and arrays switch without replacing source', async ({ page }) => {
  await mock(page); await page.goto('/projects/translated-project'); await language(page, 'Deutsch')
  await expect(page.locator('.project-text h1')).toHaveText('Neues Projekt')
  await expect(page.locator('.project-subtitle')).toHaveText('Eigene Übersetzung')
  await expect(page.locator('.project-content')).toContainText('Erster Absatz')
  await expect(page.locator('.project-content')).toContainText('Zweiter Absatz')
  await expect(page.locator('blockquote')).toHaveText('Wunderbare Musik')
  await expect(page.locator('cite')).toHaveText('Reviewer Name')
  await expect(page.locator('.project-date-location')).toContainText('Vienna')
  await language(page, 'English'); await expect(page.locator('.project-text h1')).toHaveText('New project')
  await expect(page.locator('.project-content')).toContainText('First paragraph')
})
test('media transformations preserve generated translations', async ({ page }) => {
  await mock(page); await page.goto('/media'); await language(page, 'Deutsch')
  await page.locator('.media').getByText('VIDEOS', { exact: true }).first().click()
  await expect(page.locator('.video-title')).toHaveText('Neues Video')
  await page.locator('.media').getByText('FOTOS', { exact: true }).first().click()
  await expect(page.locator('.photo-group-title')).toHaveText('Neue Galerie')
  await page.locator('.media').getByText('NEUIGKEITEN', { exact: true }).first().click()
  await expect(page.locator('.news-title')).toHaveText('Neue Nachricht')
  await page.locator('.news-preview').click(); await expect(page.locator('.news-rich')).toContainText('Neue Geschichte')
})
test('admin can inspect failures, retry and preserve edited variants', async ({ page }, testInfo) => {
  const writes = await mock(page, { admin: true }); await page.goto('/admin')
  const panel = page.locator('.translation-panel'); await panel.locator('summary').click()
  await expect(panel).toContainText('Gemini 할당량을 초과했습니다.')
  await panel.getByRole('button', { name: '번역 재시도' }).click(); await expect(panel).toContainText('번역을 다시 대기열에 등록했습니다.')
  await panel.getByRole('button', { name: '번역 수정' }).first().click()
  await panel.getByLabel('Deutsch', { exact: true }).fill('Redaktionelles Projekt')
  await panel.getByRole('button', { name: '번역 저장' }).click()
  await expect(panel).toContainText('Redaktionelles Projekt')
  expect(writes[0].path).toBe('/api/admin/translations/2/retry')
  expect(writes[1].body).toEqual({ version: 2, en: 'New project', de: 'Redaktionelles Projekt' })
  await page.screenshot({ path: testInfo.outputPath('translation-admin.png'), fullPage: true })
})
