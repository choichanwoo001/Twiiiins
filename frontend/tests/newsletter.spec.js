import { test, expect } from '@playwright/test'

const news = { id: 42, date: '2026-10-07', title: 'English announcement', titleDe: 'Deutsche Neuigkeiten', bodyEn: '<p>Hello <strong>friends</strong></p>', bodyDe: '<p>Hallo <strong>Freunde</strong></p>', description: 'Legacy description', imageUrls: [], videoUrls: ['https://example.com/video'], status: 'DRAFT', source: 'NEWSLETTER', version: 0, displayOrder: 0 }
async function mocks(page, { available = true, admin = false } = {}) {
  await page.addInitScript(() => localStorage.setItem('twiiiins-cookie-preferences', JSON.stringify({ version: 1, media: false, savedAt: Date.now() })))
  let saved = { ...news }, posted = []
  let manualNews = [{ id: 7, date: '2026-10-07', title: 'Regular News', description: 'Regular description', imageUrls: [], source: 'NEWS', status: 'PUBLISHED', version: 0 }]
  await page.route('**/api/**', async route => {
    const request = route.request(), path = new URL(request.url()).pathname
    if (!path.startsWith('/api/')) return route.continue()
    if (request.method() === 'POST') posted.push({ path, data: request.headers()['content-type']?.includes('multipart/form-data') ? null : request.postDataJSON() })
    let data = []
    if (path === '/api/admin/newsletter/preview') { const draft = request.postDataJSON(); data = { title: draft.language === 'de' ? draft.titleDe : draft.title, html: `<html><body><h1>${draft.language === 'de' ? draft.titleDe : draft.title}</h1></body></html>` } }
    if (path === '/api/admin/newsletter/images') data = ['/uploads/test-newsletter.png']
    if (path === '/api/newsletter/status') data = { available }
    if (path === '/api/auth/me') {
      if (!admin) return route.fulfill({ status: 401, json: { success: false } })
      data = { username: 'test-admin' }
    }
    if (path === '/api/media/news') {
      if (request.method() === 'POST') { const item = { ...request.postDataJSON(), id: 8, source: 'NEWS', status: 'PUBLISHED', version: 0 }; manualNews.push(item); data = item }
      else data = [...manualNews, { ...saved, status: 'PUBLISHED' }]
    }
    if (path === '/api/admin/news') {
      if (request.method() === 'POST') saved = { ...saved, ...request.postDataJSON(), id: 42 }
      data = request.method() === 'GET' ? [saved] : saved
    }
    if (path === '/api/admin/news/42' && request.method() === 'PUT') { saved = { ...saved, ...request.postDataJSON(), version: saved.version + 1 }; data = saved }
    if (path === '/api/admin/news/42/preview') data = { title: saved.title, html: saved.bodyEn }
    if (path === '/api/admin/news/42/publish') { saved.status = 'PUBLISHED'; data = saved }
    if (path === '/api/admin/news/42/send') data = { id: 1, newsId: 42 }
    if (path === '/api/admin/newsletter/settings') data = { available, paused: false, perMinute: 10, dailyLimit: 300, testRecipients: ['admin@example.com'] }
    if (path === '/api/admin/newsletter/subscribers') data = [
      { id: 1, email: 'one@example.com', language: 'en', status: 'ACTIVE', createdAt: '2026-10-07T03:00:00Z' },
      { id: 2, email: 'two@example.com', language: 'de', status: 'ACTIVE', createdAt: '2026-10-07T03:00:00Z' },
      { id: 3, email: 'pending@example.com', language: 'de', status: 'PENDING', createdAt: '2026-10-07T03:00:00Z' },
      { id: 4, email: 'excluded@example.com', language: 'en', status: 'EXCLUDED', createdAt: '2026-10-07T03:00:00Z' }
    ]
    return route.fulfill({ json: { success: true, data } })
  })
  return posted
}

test('newsletter photos upload without saving an incomplete draft and actions are separated', async ({ page }, testInfo) => {
  const posted = await mocks(page, { admin: true })
  await page.route('**/uploads/test-newsletter.png', route => route.fulfill({ contentType: 'image/png', body: Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jfsUAAAAASUVORK5CYII=', 'base64') }))
  await page.addInitScript(() => localStorage.setItem('token', 'test-token'))
  await page.goto('/admin')
  await page.getByRole('tab', { name: '뉴스레터 관리 메뉴' }).click()
  const admin = page.locator('.newsletter-admin')
  await admin.getByLabel('안내 버튼 주소 · 두 언어 공통').fill('http://example.com')
  await admin.getByLabel('사진 추가').setInputFiles({ name: 'photo.png', mimeType: 'image/png', buffer: Buffer.from('image') })
  await expect(admin.getByRole('status')).toContainText('사진을 추가했습니다')
  await expect(admin.getByLabel('영어 제목')).toHaveValue('')
  await expect(admin.getByLabel('안내 버튼 주소 · 두 언어 공통')).toHaveValue('http://example.com')
  const image = admin.locator('.photos img')
  await expect(image).toHaveAttribute('src', '/uploads/test-newsletter.png')
  await expect.poll(() => image.evaluate(img => img.naturalWidth)).toBeGreaterThan(0)
  expect(posted.some(p => p.path === '/api/admin/news')).toBe(false)
  const groups = admin.locator('.action-group')
  await expect(groups).toHaveCount(3)
  await expect(groups.nth(0).getByRole('button', { name: '저장', exact: true })).toBeVisible()
  await expect(groups.nth(1).getByRole('button', { name: 'EN 테스트 발송' })).toBeVisible()
  await expect(groups.nth(2).getByRole('button', { name: '게시 및 구독자 발송' })).toBeVisible()
  await groups.nth(2).scrollIntoViewIfNeeded()
  await page.screenshot({ path: testInfo.outputPath('newsletter-actions.png'), fullPage: true })
})

test('subscriber management explains immediate activation without a confirmation action', async ({ page }) => {
  await mocks(page, { admin: true })
  await page.addInitScript(() => localStorage.setItem('token', 'test-token'))
  await page.goto('/admin')
  await page.getByRole('tab', { name: '뉴스레터 관리 메뉴' }).click()
  const admin = page.locator('.newsletter-admin')
  await admin.getByRole('button', { name: '구독자', exact: true }).click()
  await expect(admin).toContainText('별도의 이메일 확인은 필요하지 않습니다')
  await expect(admin.getByRole('button', { name: '인증 메일 다시 보내기' })).toHaveCount(0)
})

test('Contact starts with newsletter and submits selected language and consent', async ({ page }, testInfo) => {
  const posted = await mocks(page)
  await page.goto('/contact')
  const signup = page.locator('.newsletter-signup')
  await expect(signup.getByRole('heading', { name: 'Newsletter' })).toBeVisible()
  const signupBox = await signup.boundingBox(), introBox = await page.locator('.contact-info').boundingBox()
  expect(signupBox.y).toBeLessThan(introBox.y)
  await signup.getByLabel('Email address').fill('reader@example.com')
  await signup.getByLabel('Newsletter language').selectOption('de')
  await signup.getByRole('checkbox').check()
  await signup.getByRole('button', { name: 'Subscribe', exact: true }).click()
  await expect(signup.getByRole('status')).toContainText('Thank you for subscribing')
  expect(posted.find(p => p.path.endsWith('/subscribe')).data).toEqual({ email: 'reader@example.com', language: 'de', consent: true })
  await page.screenshot({ path: testInfo.outputPath('contact-newsletter.png'), fullPage: true })
})

test('disabled SMTP shows availability notice without email form', async ({ page }) => {
  await mocks(page, { available: false }); await page.goto('/contact')
  await expect(page.locator('.newsletter-signup')).toContainText('Newsletter subscriptions are currently unavailable')
  await expect(page.locator('.newsletter-signup input[type=email]')).toHaveCount(0)
})

test('confirmation and unsubscribe only post after an explicit button click', async ({ page }) => {
  const posted = await mocks(page)
  await page.goto('/newsletter/confirm?token=test-link&lang=de')
  expect(posted).toHaveLength(0)
  await page.getByRole('button', { name: 'Abonnement bestätigen', exact: true }).click()
  await expect(page.getByRole('status')).toContainText('bestätigt')
  expect(posted[0].path).toBe('/api/newsletter/confirm')
  await page.goto('/newsletter/unsubscribe?token=test-unsubscribe&lang=en')
  expect(posted).toHaveLength(1)
  await page.getByRole('button', { name: 'Unsubscribe from newsletter', exact: true }).click()
  await expect(page.getByRole('status')).toContainText('unsubscribed')
  expect(posted[1].path).toBe('/api/newsletter/unsubscribe')
})

test('email web link opens the correct German News', async ({ page }) => {
  await mocks(page); await page.goto('/media?section=news&newsId=42&lang=de')
  await expect(page.locator('.news-details')).toBeVisible()
  const expandedNews = page.locator('.news-item').filter({ has: page.locator('.news-details') })
  await expect(expandedNews.locator('.news-title')).toHaveText('Deutsche Neuigkeiten')
  await expect(page.locator('.news-rich strong')).toHaveText('Freunde')
  await expect(page.getByRole('link', { name: /Video ansehen/ })).toHaveAttribute('href', 'https://example.com/video')
})

test('stale local administrator information cannot open management without a server token', async ({ page }) => {
  await mocks(page)
  await page.addInitScript(() => localStorage.setItem('user', JSON.stringify({ username: 'forged-admin' })))
  await page.goto('/admin'); await expect(page).toHaveURL(/\/login/)
})

test('administrator edits both languages and confirms audience before sending', async ({ page }, testInfo) => {
  const posted = await mocks(page, { admin: true })
  await page.addInitScript(() => localStorage.setItem('token', 'test-token'))
  await page.goto('/admin')
  await page.getByRole('tab', { name: '뉴스레터 관리 메뉴' }).click()
  const admin = page.locator('.newsletter-admin')
  await admin.getByRole('button', { name: '수정', exact: true }).click()
  await admin.getByLabel('영어 제목').fill('Updated English title')
  await admin.getByRole('button', { name: 'Deutsch', exact: true }).click()
  await admin.getByLabel('독일어 제목').fill('Aktualisierte Neuigkeiten')
  await expect(admin.getByRole('textbox', { name: '뉴스레터 본문' })).toContainText('Hallo')
  await admin.getByRole('button', { name: '게시 및 구독자 발송', exact: true }).click()
  await expect(page.locator('.dialog-body')).toContainText('영어 1명, 독일어 1명')
  expect(posted.filter(p => p.path.endsWith('/send'))).toHaveLength(0)
  await page.locator('.dialog-footer').getByRole('button', { name: '확인', exact: true }).click()
  await expect(admin.getByRole('status')).toContainText('발송 대기열')
  expect(posted.filter(p => p.path.endsWith('/send'))).toHaveLength(1)
  await page.screenshot({ path: testInfo.outputPath('admin-newsletter.png'), fullPage: true })
})

test('Media preserves the regular News form and links generated posts to Newsletter editing', async ({ page }, testInfo) => {
  const posted = await mocks(page, { admin: true })
  await page.addInitScript(() => localStorage.setItem('token', 'test-token'))
  await page.goto('/admin')
  await page.getByRole('tab', { name: '미디어 관리 메뉴' }).click()
  await page.getByRole('button', { name: 'News', exact: true }).click()
  const newsTab = page.locator('.news-tab')
  await expect(newsTab).toBeVisible()
  await expect(page.locator('.newsletter-admin')).toHaveCount(0)
  const form = newsTab.locator('.crud-form')
  await form.locator('input[type=date]').fill('2026-10-07')
  await form.getByPlaceholder('제목을 입력하세요').fill('New regular announcement')
  await form.getByPlaceholder('설명을 입력하세요').fill('Plain News content')
  await form.getByRole('button', { name: '등록', exact: true }).click()
  await expect(newsTab.getByRole('cell', { name: 'New regular announcement', exact: true })).toBeVisible()
  expect(posted.find(p => p.path === '/api/media/news').data).toMatchObject({ title: 'New regular announcement', description: 'Plain News content' })
  expect(posted.filter(p => p.path.endsWith('/send'))).toHaveLength(0)
  const generatedRow = newsTab.getByRole('row').filter({ hasText: 'English announcement' })
  await expect(generatedRow.getByRole('button')).toHaveCount(1)
  await page.screenshot({ path: testInfo.outputPath('admin-regular-news.png'), fullPage: true })
  const listRequest = page.waitForRequest(r => new URL(r.url()).pathname === '/api/admin/news')
  await generatedRow.getByRole('button', { name: 'Newsletter에서 수정' }).click()
  expect(new URL((await listRequest).url()).searchParams.get('source')).toBe('NEWSLETTER')
  await expect(page.getByRole('tab', { name: '뉴스레터 관리 메뉴' })).toHaveAttribute('aria-selected', 'true')
  await expect(page.locator('.newsletter-admin').getByLabel('영어 제목')).toHaveValue('English announcement')
  await expect(page.locator('.newsletter-admin').getByRole('cell', { name: 'Regular News', exact: true })).toHaveCount(0)
})

test('Newsletter subscriber list shows dates, counts, translated states and search filters', async ({ page }, testInfo) => {
  await mocks(page, { admin: true })
  await page.addInitScript(() => localStorage.setItem('token', 'test-token'))
  await page.goto('/admin')
  await page.getByRole('tab', { name: '뉴스레터 관리 메뉴' }).click()
  const admin = page.locator('.newsletter-admin')
  await admin.getByRole('button', { name: '구독자', exact: true }).click()
  await expect(admin).toContainText('전체 신청자 4명 · 구독 중 2명')
  await expect(admin.getByRole('cell', { name: '이전 인증 대기', exact: true })).toBeVisible()
  await expect(admin.getByRole('cell', { name: '구독 중', exact: true })).toHaveCount(2)
  await expect(admin.getByRole('columnheader', { name: '신청일' })).toBeVisible()
  await expect(admin.getByRole('cell', { name: /2026/ })).toHaveCount(4)
  await admin.getByLabel('수신 언어').selectOption('de')
  await admin.getByLabel('구독 상태').selectOption('PENDING')
  await admin.getByLabel('구독자 검색').fill('PENDING@')
  await expect(admin).toContainText('검색 결과 1명')
  await expect(admin.getByRole('cell', { name: 'pending@example.com', exact: true })).toBeVisible()
  await expect(admin.getByRole('cell', { name: 'two@example.com', exact: true })).toHaveCount(0)
  await page.screenshot({ path: testInfo.outputPath('admin-subscribers.png'), fullPage: true })
  await admin.getByLabel('구독자 검색').fill('missing')
  await expect(admin).toContainText('조건에 맞는 구독자가 없습니다.')
})

for (const failure of [404, 500, 'network', 'invalid']) {
  test(`status failure ${failure} stays inline and can retry`, async ({ page }) => {
    await mocks(page)
    let fail = true
    await page.route('**/api/newsletter/status', route => {
      if (!fail) return route.fulfill({ json: { success: true, data: { available: true } } })
      if (failure === 'network') return route.abort('failed')
      if (failure === 'invalid') return route.fulfill({ json: { success: true, data: {} } })
      return route.fulfill({ status: failure, json: { success: false } })
    })
    await page.goto('/contact')
    const signup = page.locator('.newsletter-signup')
    await expect(signup.getByRole('alert')).toContainText('could not check')
    await expect(signup).not.toContainText('currently unavailable')
    await expect(page.getByText('요청한 리소스를 찾을 수 없습니다.', { exact: true })).toHaveCount(0)
    fail = false
    await signup.getByRole('button', { name: 'Try again' }).click()
    await expect(signup.getByLabel('Email address')).toBeVisible()
  })
}

test('status loading and German disabled copy', async ({ page }) => {
  await mocks(page)
  await page.addInitScript(() => localStorage.setItem('twiiiins-website-language', 'de'))
  let release
  const pending = new Promise(resolve => { release = resolve })
  await page.route('**/api/newsletter/status', async route => {
    await pending
    await route.fulfill({ json: { success: true, data: { available: false } } })
  })
  await page.goto('/contact')
  const signup = page.locator('.newsletter-signup')
  await expect(signup).toContainText('Die Verfügbarkeit wird geprüft')
  release()
  await expect(signup).toContainText('derzeit nicht verfügbar')
})

test('real subscription prevents duplicates and preserves inputs on failure', async ({ page }) => {
  await mocks(page)
  let release, count = 0
  const pending = new Promise(resolve => { release = resolve })
  await page.route('**/api/newsletter/subscribe', async route => {
    count++
    await pending
    await route.fulfill({ status: 503, json: { success: false } })
  })
  await page.goto('/contact')
  const signup = page.locator('.newsletter-signup')
  await signup.getByLabel('Email address').fill('reader@example.com')
  await signup.getByRole('checkbox').check()
  await signup.getByRole('button', { name: 'Subscribe', exact: true }).click()
  await expect(signup.getByRole('button', { name: 'Sending…' })).toBeDisabled()
  await signup.locator('form').evaluate(form => form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true })))
  release()
  await expect(signup.getByRole('status')).toContainText('could not process')
  await expect(signup.getByLabel('Email address')).toHaveValue('reader@example.com')
  expect(count).toBe(1)
})


test('administrator live preview uses unsaved draft, ignores stale responses and retries inline', async ({ page }) => {
  await mocks(page, { admin: true })
  await page.addInitScript(() => localStorage.setItem('token', 'test-token'))
  const calls = [], writes = []
  let first, failure = false
  page.on('request', request => { if (request.method() === 'POST' && new URL(request.url()).pathname === '/api/admin/news') writes.push(request.url()) })
  await page.route('**/api/admin/newsletter/preview', async route => {
    const data = route.request().postDataJSON()
    calls.push(data)
    if (calls.length === 1) { first = route; return }
    if (failure) return route.fulfill({ status: 500, json: { success: false } })
    await route.fulfill({ json: { success: true, data: { title: data.title, html: `<h1>${data.title}</h1><p>${data.eventWhenEn || ''}</p>` } } })
  })
  await page.goto('/admin')
  await page.getByRole('tab', { name: '뉴스레터 관리 메뉴' }).click()
  await expect.poll(() => calls.length).toBe(1)
  await page.getByLabel('영어 제목').fill('Unsaved version one')
  await page.getByLabel('영어 제목').fill('Newest unsaved title')
  await page.getByLabel('영어 공연 일시').fill('Friday at 8pm')
  const frame = page.frameLocator('iframe[title="발송 메일 실시간 미리보기"]')
  await expect(frame.getByRole('heading')).toHaveText('Newest unsaved title')
  await expect.poll(() => calls.at(-1)?.eventWhenEn).toBe('Friday at 8pm')
  expect(calls.at(-1).title).toBe('Newest unsaved title')
  await first.fulfill({ json: { success: true, data: { title: 'Stale', html: '<h1>Stale</h1>' } } })
  await expect(frame.getByRole('heading')).toHaveText('Newest unsaved title')
  failure = true
  await page.getByLabel('영어 제목').fill('After retry')
  const preview = page.locator('.mail-preview')
  await expect(preview.getByRole('alert')).toBeVisible()
  await expect(frame.getByRole('heading')).toHaveText('Newest unsaved title')
  failure = false
  await preview.getByRole('button', { name: '다시 시도' }).click()
  await expect(frame.getByRole('heading')).toHaveText('After retry')
  await expect(preview.getByRole('alert')).toHaveCount(0)
  expect(writes).toEqual([])
})
