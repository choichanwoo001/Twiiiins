import { test, expect, publicApi } from './support/fixtures'
import AxeBuilder from '@axe-core/playwright'

const pages = ['/', '/about', '/projects', '/concerts', '/media', '/shop', '/contact', '/privacy', '/login']
for (const path of pages) {
  test(`public route survives direct access and reload: ${path}`, async ({ page }) => {
    await publicApi(page)
    await page.goto(path); await expect(page.locator('#app')).not.toBeEmpty()
    await page.reload(); await expect(page.locator('#app')).not.toBeEmpty()
    await expect(page.locator('main, h1, .home').first()).toBeVisible()
  })
}
test('history and navigation keep the correct page', async ({ page, isMobile }) => {
  await publicApi(page); await page.goto('/shop')
  if (isMobile) await page.locator('.hamburger-button').click()
  await page.getByRole('link', { name: 'ABOUT', exact: true }).filter({ visible: true }).click()
  await expect(page).toHaveURL(/\/about$/); await page.goBack(); await expect(page).toHaveURL(/\/shop$/)
  await page.goForward(); await expect(page).toHaveURL(/\/about$/)
})
test('shop has a usable email order link in both languages', async ({ page }) => {
  await publicApi(page); await page.goto('/shop')
  await expect(page.getByRole('heading', { name: 'Only The Beginning' })).toBeVisible()
  await expect(page.locator('.album-price')).toHaveText('€13')
  const link = page.locator('.order-button'); const url = new URL(await link.getAttribute('href'))
  expect(url.protocol).toBe('mailto:'); expect(url.pathname).toBe('contact@twiiiins.com')
  expect(url.searchParams.get('subject')).toBe('TWIIIINS – Only The Beginning CD order')
  await page.locator('.website-language summary').click(); await page.getByRole('button', { name: 'Deutsch', exact: true }).click()
  await expect(link).toContainText('Per E-Mail bestellen'); await page.reload()
  await expect(page.locator('html')).toHaveAttribute('lang', 'de')
})
test('unknown project is handled without a browser crash', async ({ page }) => {
  await publicApi(page)
  await page.route('**/api/projects/slug/missing', route => route.fulfill({ status: 404, json: { success: false, error: { message: 'Not found' } } }))
  await page.goto('/projects/missing'); await expect(page.locator('.project-detail')).toBeVisible()
  await expect(page.locator('.project-detail')).not.toBeEmpty()
})
test('principal page landmarks and forms have no serious accessibility violations', async ({ page }) => {
  await publicApi(page)
  for (const path of ['/login', '/contact', '/shop', '/privacy']) {
    await page.goto(path)
    const result = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze()
    expect(result.violations.filter(v => ['critical', 'serious'].includes(v.impact)), path).toEqual([])
  }
})
for (const width of [320, 390, 768, 1366]) {
  test(`layout contains its content at ${width}px`, async ({ page }) => {
    await publicApi(page)
    await page.setViewportSize({ width, height: 900 })
    for (const path of ['/shop', '/contact', '/privacy', '/projects', '/concerts']) {
      await page.goto(path)
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth + 1), `${path} at ${width}`).toBe(true)
    }
  })
}

test('external media requires consent, persists the choice and supports revocation', async ({ page }) => {
  await publicApi(page, { '/api/media/videos': [{ id: 1, title: 'Consent video', embedUrl: 'https://www.youtube.com/embed/abcdefghijk' }] })
  let externalRequests = 0
  await page.route('https://www.youtube.com/**', route => { externalRequests++; return route.fulfill({ contentType: 'text/html', body: '<html><body>Local video fixture</body></html>' }) })
  await page.goto('/media?section=videos'); await expect(page.getByRole('button', { name: 'Enable external media' })).toBeVisible()
  expect(externalRequests).toBe(0); await expect(page.locator('iframe')).toHaveCount(0)
  await page.getByRole('button', { name: 'Enable external media' }).click()
  const dialog = page.getByRole('dialog'); await expect(dialog).toBeVisible()
  const result = await new AxeBuilder({ page }).include('.website-dialog').withTags(['wcag2a', 'wcag2aa']).analyze()
  expect(result.violations.filter(v => ['critical', 'serious'].includes(v.impact))).toEqual([])
  await dialog.getByRole('button', { name: 'Accept all' }).click()
  await expect(page.locator('iframe')).toHaveCount(1); await expect.poll(() => externalRequests).toBe(1)
  await page.reload(); await expect(page.locator('iframe')).toHaveCount(1)
  const settings = page.getByRole('button', { name: 'Cookie settings', exact: true })
  await settings.click(); await dialog.getByRole('button', { name: 'Reject optional' }).click()
  await expect(page.locator('iframe')).toHaveCount(0); await expect(settings).toBeFocused()
  await settings.click(); await page.keyboard.press('Escape')
  await expect(dialog).not.toBeVisible(); await expect(settings).toBeFocused()
})
