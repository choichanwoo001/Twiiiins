import { test, expect } from './support/fixtures'

const shapes = [[1200, 800], [800, 1200], [900, 900], [2000, 400], [400, 1600], [1200, 800]]

test('photo gallery fills its width, contains originals and reflows without overlap', async ({ page, isMobile }) => {
  const errors = []
  page.on('pageerror', error => errors.push(error.message))
  await page.addInitScript(() => { if (window !== window.top) return; localStorage.setItem('twiiiins-cookie-preferences', JSON.stringify({ version: 1, media: false, savedAt: Date.now() })) })
  await page.route('**/api/**', route => {
    const path = new URL(route.request().url()).pathname
    if (!path.startsWith('/api/')) return route.continue()
    const data = path === '/api/media/photo-groups'
      ? [{ id: 1, title: 'Mixed photo orientations', photos: shapes.map((_, i) => ({ id: i + 1, imageUrl: `/gallery-layout/photo-${i}.svg`, thumbnailUrl: '/gallery-layout/thumbnail.svg' })) }]
      : []
    return route.fulfill({ json: { success: true, data } })
  })
  await page.route('**/gallery-layout/*.svg', route => {
    const index = Number(new URL(route.request().url()).pathname.match(/photo-(\d+)/)?.[1] ?? 0)
    const [width, height] = shapes[index]
    return route.fulfill({ contentType: 'image/svg+xml', body: `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}"><rect width="100%" height="100%" fill="#cec9c3"/></svg>` })
  })
  await page.goto('/media?section=photos')

  for (const width of isMobile ? [390, 320] : [1920, 1366, 800]) {
    await page.setViewportSize({ width, height: 900 })
    const images = page.locator('.photo-item img')
    await expect(images).toHaveCount(shapes.length)
    await images.evaluateAll(items => items.forEach(image => { image.loading = 'eager' }))
    await expect.poll(() => images.evaluateAll(items => items.every(image => image.complete && image.naturalWidth > 0))).toBe(true)
    await expect.poll(() => images.evaluateAll(items => items.every(image => {
      const cell = image.parentElement.getBoundingClientRect()
      const photo = image.getBoundingClientRect()
      return cell.height >= photo.height && cell.height - photo.height < 16
    }))).toBe(true)

    const grid = await page.locator('.photos-grid').boundingBox()
    const cells = await images.evaluateAll(items => items.map(image => {
      const r = image.parentElement.getBoundingClientRect()
      const photo = image.getBoundingClientRect()
      return { x: r.x, y: r.y, right: r.right, bottom: r.bottom, width: r.width, ratio: photo.width / photo.height, naturalRatio: image.naturalWidth / image.naturalHeight, fit: getComputedStyle(image).objectFit, source: image.currentSrc }
    }))
    expect(Math.abs(Math.max(...cells.map(cell => cell.right)) - grid.x - grid.width)).toBeLessThan(1)
    expect(Math.max(...cells.map(cell => cell.width)) - Math.min(...cells.map(cell => cell.width))).toBeLessThan(1)
    for (let i = 0; i < cells.length; i++) {
      expect(Math.abs(cells[i].ratio - cells[i].naturalRatio)).toBeLessThan(.01)
      expect(cells[i].fit).toBe('contain')
      expect(cells[i].source).not.toContain('thumbnail')
      for (let j = i + 1; j < cells.length; j++) {
        const a = cells[i], b = cells[j]
        expect(a.x < b.right - .5 && a.right > b.x + .5 && a.y < b.bottom - .5 && a.bottom > b.y + .5).toBe(false)
      }
    }
    expect(await page.locator('html').evaluate(element => element.scrollWidth > element.clientWidth)).toBe(false)
  }

  // Returning to Photos creates fresh observed images, rather than stale cells.
  const navigation = page.locator(isMobile ? '.mobile-nav' : '.media-nav')
  await navigation.getByText('MUSIC', { exact: true }).click()
  await expect(page.locator('.photo-item')).toHaveCount(0)
  await navigation.getByText('PHOTOS', { exact: true }).click()
  await expect.poll(() => page.locator('.photo-item').evaluateAll((items, count) => items.length === count && items.every(item => item.style.gridRowEnd.startsWith('span ')), shapes.length)).toBe(true)
  expect(errors).toEqual([])
})
