import { test, expect, emptyApiResponse } from './support/fixtures'

test('administrator create, edit and delete show pending, failure and successful retry without losing input', async ({ page }) => {
  await page.addInitScript(() => { if (window !== window.top) return; localStorage.setItem('token', 'test-token') })
  let contacts = [], release
  page.on('dialog', dialog => dialog.accept())
  await page.route('**/api/**', async route => {
    const request = route.request(), path = new URL(request.url()).pathname, method = request.method()
    if (!path.startsWith('/api/')) return route.continue()
    if (path === '/api/auth/me') return route.fulfill({ json: { success: true, data: { username: 'admin' } } })
    if (path === '/api/media/contacts' && method === 'GET') return route.fulfill({ json: { success: true, data: contacts } })
    if (path.startsWith('/api/media/contacts') && method !== 'GET') {
      const success = await new Promise(resolve => { release = resolve })
      if (!success) return route.fulfill({ status: 500, json: { success: false, error: { message: '테스트 저장 또는 삭제 실패' } } })
      if (method === 'POST') contacts = [{ ...request.postDataJSON(), id: 1 }]
      if (method === 'PUT') contacts = [{ ...request.postDataJSON(), id: 1 }]
      if (method === 'DELETE') contacts = []
      return route.fulfill({ json: { success: true, data: contacts[0] ?? 'Deleted' } })
    }
    return route.fulfill({ json: { success: true, data: emptyApiResponse(request) } })
  })
  await page.goto('/admin'); await page.getByRole('tab', { name: '연락처 관리 메뉴' }).click()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true)
  const form = page.locator('.crud-form'), inputs = form.locator('input')
  await inputs.nth(0).fill('Status Contact'); await inputs.nth(1).fill('Booking'); await inputs.nth(2).fill('status@example.com')
  const status = page.getByRole('region', { name: '관리자 작업 상태' })
  const table = page.getByRole('table', { name: '전체 목록 데이터 테이블' })
  for (const [action, start] of [
    ['등록', () => form.getByRole('button', { name: '등록', exact: true }).click()],
    ['수정', async () => { await table.getByRole('button', { name: 'Status Contact 수정' }).click(); await inputs.nth(1).fill('Updated Booking'); await form.getByRole('button', { name: '수정', exact: true }).click() }],
    ['삭제', () => table.getByRole('button', { name: 'Status Contact 삭제' }).click()],
  ]) {
    await start()
    await expect(status).toContainText(`연락처 ${action} · 처리 중…`)
    if (action !== '삭제') await expect(form.getByRole('button', { name: '저장 중…' })).toBeDisabled()
    release(false)
    await expect(status.getByRole('alert')).toContainText(`연락처 ${action} · 실패`)
    if (action !== '등록') await expect(table.getByRole('cell', { name: 'Status Contact', exact: true })).toBeVisible()
    if (action !== '삭제') await expect(inputs.nth(0)).toHaveValue('Status Contact')
    if (action === '삭제') await table.getByRole('button', { name: 'Status Contact 삭제' }).click()
    else await form.getByRole('button', { name: action, exact: true }).click()
    await expect(status).toContainText(`연락처 ${action} · 처리 중…`)
    release(true)
    await expect(status).toContainText(`연락처 ${action} · 성공`)
    await expect(status.getByRole('alert')).toHaveCount(0)
    if (action === '삭제') await expect(table.getByRole('cell', { name: 'Status Contact', exact: true })).toHaveCount(0)
    else await expect(table.getByRole('cell', { name: 'Status Contact', exact: true })).toBeVisible()
  }
})

test('photo deletion status stays visible inside the open management modal', async ({ page }) => {
  await page.addInitScript(() => { if (window !== window.top) return; localStorage.setItem('token', 'test-token') })
  let release
  const group = { id: 1, title: 'Status Gallery', photos: [{ id: 1, imageUrl: '/Website_logo.jpg', altText: 'Status Photo' }] }
  await page.route('**/api/**', async route => {
    const request = route.request(), path = new URL(request.url()).pathname
    if (!path.startsWith('/api/')) return route.continue()
    let data
    if (path === '/api/auth/me') data = { username: 'admin' }
    else if (path === '/api/media/photo-groups') data = [group]
    else if (path === '/api/media/photos/groups/1') data = group
    else if (path === '/api/media/photos/1' && request.method() === 'DELETE') {
      const success = await new Promise(resolve => { release = resolve })
      if (!success) return route.fulfill({ status: 500, json: { success: false, error: { message: '사진 삭제 실패' } } })
      group.photos = []; data = 'Deleted'
    } else data = emptyApiResponse(request)
    return route.fulfill({ json: { success: true, data } })
  })
  await page.goto('/admin'); await page.getByRole('tab', { name: '미디어 관리 메뉴' }).click()
  await page.getByRole('button', { name: 'Photos', exact: true }).click()
  await page.getByRole('button', { name: 'Status Gallery 사진 관리' }).click()
  const modal = page.locator('.modal-content').filter({ hasText: 'Status Gallery - 사진 관리' })
  await modal.getByRole('button', { name: '삭제', exact: true }).click()
  await page.locator('.dialog-overlay').getByRole('button', { name: '확인', exact: true }).click()
  const status = modal.getByRole('region', { name: '관리자 작업 상태' })
  await expect(status).toContainText('사진 삭제 · 처리 중…')
  expect(await status.evaluate(element => {
    const box = element.getBoundingClientRect()
    return element.contains(document.elementFromPoint(box.x + box.width / 2, box.y + box.height / 2))
  })).toBe(true)
  release(false)
  await expect(page.locator('.dialog-overlay')).toContainText('오류')
  await page.locator('.dialog-overlay').getByRole('button', { name: '확인', exact: true }).click()
  await expect(status).toContainText('사진 삭제 · 실패')
  await expect(modal.locator('.photo-item')).toHaveCount(1)
  await modal.getByRole('button', { name: '삭제', exact: true }).click()
  await page.locator('.dialog-overlay').getByRole('button', { name: '확인', exact: true }).click()
  await expect(status).toContainText('사진 삭제 · 처리 중…')
  release(true)
  await expect(status).toContainText('사진 삭제 · 성공')
  await expect(modal.locator('.photo-item')).toHaveCount(0)
})
