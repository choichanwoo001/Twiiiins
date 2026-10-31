import { test, expect } from '../support/fixtures'
import { execFileSync } from 'node:child_process'
import { resolve } from 'node:path'

const png = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAABAAAAAMCAIAAADkharWAAAAGklEQVR4nGNsjHVnIAUwkaSaYVQDcWA4BCsAvOYBPcyAXQ4AAAAASUVORK5CYII=', 'base64')
let headers
test.beforeAll(async ({ playwright }, workerInfo) => {
  const request = await playwright.request.newContext({ baseURL: workerInfo.project.use.baseURL })
  const login = await request.post('/api/auth/login', { data: { username: 'test-admin', password: 'test-password-only' } })
  expect(login.ok(), await login.text()).toBe(true)
  headers = { Authorization: `Bearer ${(await login.json()).data.token}` }
  await request.dispose()
})
test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => { if (window !== window.top) return; localStorage.setItem('twiiiins-cookie-preferences', JSON.stringify({ version: 1, media: false, savedAt: Date.now() })) })
})
async function call(request, method, path, data, options = {}) {
  const response = await request[method](path, { headers, ...(data === undefined ? {} : { data }), ...options })
  expect(response.ok(), `${method} ${path}: ${await response.text()}`).toBe(true)
  const payload = await response.json()
  return Object.hasOwn(payload, 'data') ? payload.data : payload
}
const resources = [
  { name: 'Project', path: '/api/projects', field: 'title', input: { title: 'Hello Project', urlSlug: 'journey-project', premiereDate: '2026-10-31', location: 'Berlin', descriptions: ['Hello audience'], reviews: [{ text: 'Great concert', source: 'Test Press' }] }, url: '/projects/journey-project' },
  { name: 'Concert', path: '/api/concerts', field: 'name', input: { name: 'Journey Concert', location: 'Berlin', date: '2099-10-31', isPast: false }, url: '/concerts' },
  { name: 'Music', path: '/api/media/music', field: 'title', input: { title: 'Journey Music', artist: 'TWIIIINS' }, url: '/media?section=music' },
  { name: 'Video', path: '/api/media/videos', field: 'title', input: { title: 'Journey Video', embedUrl: 'https://www.youtube.com/embed/abcdefghijk' }, url: '/media?section=videos' },
  { name: 'Equipment', path: '/api/media/equipment', field: 'name', input: { name: 'Journey Equipment' } },
  { name: 'Contact', path: '/api/media/contacts', field: 'name', input: { name: 'Journey Contact', email: 'journey@example.com', role: 'Violin' }, url: '/contact' },
  { name: 'Download', path: '/api/media/download-files', field: 'name', input: { name: 'Journey Rider', downloadSource: 'upload' }, url: '/contact' },
  { name: 'News', path: '/api/media/news', field: 'title', input: { title: 'Journey News', date: '2026-10-31', description: 'Journey description', imageUrls: [] }, url: '/media?section=news' },
]
async function expectPublicContent(page, resource, title) {
  const text = page.getByText(title, { exact: true }).first()
  if (resource.name === 'Video') {
    // The existing mobile design hides video metadata but displays the video card.
    await expect(text).toHaveText(title)
    await expect(page.locator('.video-item').filter({ has: text })).toBeVisible()
  } else await expect(text).toBeVisible()
}
for (const resource of resources) {
  test(`real ${resource.name} CRUD persists across fresh requests and page reloads`, async ({ request, page }) => {
    const input = { ...resource.input }
    if (resource.name === 'Music' || resource.name === 'Download') {
      const image = resource.name === 'Music'
      const uploaded = await call(request, 'post', image ? '/api/upload/image' : '/api/upload/file', undefined, {
        multipart: { file: { name: image ? 'cover.png' : 'rider.pdf', mimeType: image ? 'image/png' : 'application/pdf', buffer: image ? png : Buffer.from('%PDF-1.4\nTest rider\n%%EOF') } },
      })
      input[image ? 'coverUrl' : 'fileUrl'] = uploaded.url
    }
    const created = await call(request, 'post', resource.path, input)
    const path = `${resource.path}/${created.id}`
    try {
      const read = await call(request, 'get', path)
      expect(read[resource.field]).toBe(resource.input[resource.field])
      if (resource.url) {
        await page.goto(resource.url); await expectPublicContent(page, resource, input[resource.field])
        await page.reload(); await expectPublicContent(page, resource, input[resource.field])
      }
      const value = `${resource.input[resource.field]} Updated`
      if (resource.name === 'News') {
        await expect.poll(async () => (await call(request, 'get', `/api/admin/translations?kind=news&contentId=${created.id}`))
          .items.every(row => row.state === 'COMPLETED')).toBe(true)
      }
      const latest = await call(request, 'get', path)
      await call(request, 'put', path, { ...input, [resource.field]: value, ...(resource.name === 'News' ? { version: latest.version } : {}) })
      expect((await call(request, 'get', path))[resource.field]).toBe(value)
      if (resource.url) { await page.reload(); await expectPublicContent(page, resource, value) }
    } finally { await call(request, 'delete', path) }
    expect((await request.get(path)).status()).toBe(404)
    if (resource.url && resource.name !== 'Project') {
      await page.reload(); await expect(page.getByText(`${resource.input[resource.field]} Updated`, { exact: true })).toHaveCount(0)
    }
  })
}

test('administrator UI writes a concert, survives reload, then revokes its session', async ({ page, request }) => {
  await page.goto('/admin'); await expect(page).toHaveURL(/\/login/)
  await page.getByLabel('Username').fill('test-admin'); await page.getByLabel('Password').fill('test-password-only')
  await page.getByRole('button', { name: 'Login', exact: true }).click(); await expect(page).toHaveURL(/\/admin$/)
  await page.getByRole('tab', { name: '콘서트 관리 메뉴' }).click()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true)
  const form = page.locator('.concert-form')
  await form.locator('input[type=date]').fill('2099-12-01')
  await form.locator('.form-group').filter({ has: page.locator('label', { hasText: '장소 *' }) }).locator('input').fill('Test Hall')
  await form.locator('.form-group').filter({ has: page.locator('label', { hasText: '이름 *' }) }).locator('input').fill('UI Concert')
  const response = page.waitForResponse(r => r.url().endsWith('/api/concerts') && r.request().method() === 'POST')
  await form.getByRole('button', { name: '등록', exact: true }).click()
  const saved = (await (await response).json()).data
  try {
    await expect(page.getByRole('cell', { name: 'UI Concert', exact: true })).toBeVisible()
    await page.reload(); await page.getByRole('tab', { name: '콘서트 관리 메뉴' }).click()
    await expect(page.getByRole('cell', { name: 'UI Concert', exact: true })).toBeVisible()
    await page.getByRole('button', { name: '로그아웃' }).click(); await expect(page).toHaveURL(/\/login$/)
    await page.goto('/admin'); await expect(page).toHaveURL(/\/login/)
  } finally { await call(request, 'delete', `/api/concerts/${saved.id}`) }
})

test('real photo group uploads two images, updates metadata and cascades deletion', async ({ request, page }) => {
  const group = await call(request, 'post', '/api/media/photos/groups', { title: 'Journey Gallery' })
  const groupPath = `/api/media/photos/groups/${group.id}`
  let photos
  try {
    // APIRequestContext multipart accepts repeated fields through FormData.
    const form = new FormData()
    form.append('files', new File([png], 'landscape.png', { type: 'image/png' }))
    form.append('files', new File([png], 'portrait.png', { type: 'image/png' }))
    photos = await call(request, 'post', `${groupPath}/photos`, undefined, { multipart: form })
    expect(photos).toHaveLength(2)
    for (const photo of photos) expect((await request.get(photo.imageUrl)).ok()).toBe(true)
    await call(request, 'put', `/api/media/photos/${photos[0].id}`, { altText: 'Journey portrait' })
    await page.goto('/media?section=photos')
    await expect.poll(() => page.locator('.photo-item img').evaluateAll(imgs => imgs.filter(i => i.complete && i.naturalWidth > 0).length)).toBe(2)
    await page.reload(); await expect(page.locator('.photo-item img')).toHaveCount(2)
    await call(request, 'delete', `/api/media/photos/${photos[1].id}`)
    expect(await call(request, 'get', `${groupPath}/photos`)).toHaveLength(1)
  } finally { await call(request, 'delete', groupPath) }
  for (const photo of photos ?? []) expect((await request.get(`/api/media/photos/${photo.id}`)).status()).toBe(404)
})

test('subscription reaches SMTP and unsubscribe excludes the recipient from publication', async ({ request, page }) => {
  const email = `reader-${Date.now()}@example.com`
  await page.goto('/contact'); await page.getByLabel('Email address').fill(email)
  await page.getByLabel('Newsletter language').selectOption('de')
  await page.locator('.newsletter-signup input[type=checkbox]').check()
  await page.locator('.newsletter-signup').getByRole('button', { name: 'Subscribe', exact: true }).click()
  await expect(page.locator('.newsletter-signup [role=status]')).toContainText('Thank you')
  let message
  await expect.poll(async () => {
    const response = await request.get('http://127.0.0.1:18025/api/v1/messages')
    const messages = (await response.json()).messages
    message = messages.find(m => m.To.some(to => to.Address === email))
    return !!message
  }).toBe(true)
  const mail = await (await request.get(`http://127.0.0.1:18025/api/v1/message/${message.ID}`)).json()
  expect(mail.Subject).toContain('Willkommen'); expect(mail.HTML).toContain('unsubscribe?token=')
  const unsubscribe = mail.HTML.match(/href="([^"]*\/newsletter\/unsubscribe\?[^"]+)"/)[1].replaceAll('&amp;', '&')
  await page.goto(unsubscribe)
  await page.getByRole('button', { name: /abmelden|Unsubscribe/ }).click()
  await expect.poll(async () => (await call(request, 'get', `/api/admin/newsletter/subscribers?search=${email}`))[0]?.status).toBe('UNSUBSCRIBED')
  const activeEmail = `active-${Date.now()}@example.com`
  await call(request, 'post', '/api/newsletter/subscribe', { email: activeEmail, language: 'en', consent: true })
  const post = await call(request, 'post', '/api/admin/news', { title: 'Hello newsletter', bodyEn: '<p>Hello audience</p>', date: '2026-10-31' })
  try {
    await expect.poll(async () => {
      const { items } = await call(request, 'get', `/api/admin/translations?kind=news&contentId=${post.id}`)
      return items.length > 0 && items.every(row => row.state === 'COMPLETED')
    }).toBe(true)
    await call(request, 'post', `/api/admin/news/${post.id}/publish`)
    const current = await call(request, 'get', `/api/admin/news/${post.id}`)
    const first = await call(request, 'post', `/api/admin/news/${post.id}/send`, { version: current.version })
    const second = await call(request, 'post', `/api/admin/news/${post.id}/send`, { version: current.version })
    expect(second.id).toBe(first.id)
    const deliveries = await call(request, 'get', `/api/admin/newsletter/mailings/${first.id}/deliveries`)
    expect(deliveries.some(d => d.email === email)).toBe(false)
    expect(deliveries.filter(d => d.email === activeEmail)).toHaveLength(1)
    await expect.poll(async () => (await call(request, 'get', `/api/admin/newsletter/mailings/${first.id}/deliveries`))
      .find(d => d.email === activeEmail)?.status).toBe('ACCEPTED')
    const inbox = (await (await request.get('http://127.0.0.1:18025/api/v1/messages')).json()).messages
    expect(inbox.filter(m => m.Subject === 'Hello newsletter' && m.To.some(to => to.Address === activeEmail))).toHaveLength(1)
    await page.goto('/media?section=news'); await expect(page.getByText('Hello newsletter', { exact: true }).first()).toBeVisible()
    await call(request, 'delete', `/api/admin/news/${post.id}`)
    await page.reload()
    await expect(page.getByText('Hello newsletter', { exact: true })).toHaveCount(0)
    expect((await call(request, 'get', '/api/media/news?title=Hello')).some(item => item.id === post.id)).toBe(false)
    expect((await call(request, 'get', '/api/admin/newsletter/mailings')).some(item => item.id === first.id)).toBe(true)
    expect((await call(request, 'get', `/api/admin/newsletter/mailings/${first.id}/deliveries`)).map(item => item.id)).toEqual(deliveries.map(item => item.id))
    await page.goto(`/media?section=news&newsId=${post.id}`)
    await expect(page.getByText('Hello newsletter', { exact: true }).first()).toBeVisible()
    await expect(page.getByText('Hello audience', { exact: true })).toBeVisible()
  } finally { await call(request, 'delete', `/api/admin/news/${post.id}`) }
})

test('translation completes through HTTP and manual text survives a backend restart', async ({ request, page }) => {
  const project = await call(request, 'post', '/api/projects', { title: 'Hello Project', urlSlug: 'translated-journey', premiereDate: '2026-10-31' })
  try {
    let row
    await expect.poll(async () => {
      const result = await call(request, 'get', `/api/admin/translations?kind=projects&contentId=${project.id}`)
      row = result.items.find(r => r.field === 'title'); return row?.state
    }).toBe('COMPLETED')
    await call(request, 'put', `/api/admin/translations/${row.id}`, { version: row.version, en: 'Hello Project', de: 'Manuell bearbeitet' })
    await page.goto('/projects/translated-journey'); await page.locator('.website-language summary').click()
    await page.getByRole('button', { name: 'Deutsch', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'Manuell bearbeitet' })).toBeVisible()
    if (process.env.TEST_SUITE === 'nightly') {
      test.setTimeout(180000)
      if (process.env.TWIIIINS_TEST_STACK !== '1') throw new Error('Restart tests require the isolated stack runner')
      execFileSync(process.platform === 'win32' ? 'python' : 'python3', [resolve('../scripts/test-stack.py'), 'restart'], { stdio: 'pipe', timeout: 60000 })
      await expect.poll(async () => { try { return (await request.get('/actuator/health')).ok() } catch { return false } }, { timeout: 120000 }).toBe(true)
      await page.reload(); await expect(page.getByRole('heading', { name: 'Manuell bearbeitet' })).toBeVisible()
    }
  } finally { await call(request, 'delete', `/api/projects/${project.id}`) }
})
