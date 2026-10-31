import { test, expect, publicApi } from './support/fixtures'

test('album artwork retains its crop and aspect ratio', async ({ page }) => {
  await publicApi(page); await page.goto('/shop')
  await expect.poll(() => page.locator('.album-artwork img').evaluate(img => img.complete && img.naturalWidth > 0)).toBe(true)
  await expect(page.locator('.album-artwork')).toHaveScreenshot('album.png', { style: '.navbar, .legal-footer, .sns-links { visibility: hidden !important; }' })
})

test('mixed gallery retains its masonry geometry', async ({ page }) => {
  const shapes = [[1200, 800], [800, 1200], [900, 900], [2000, 400], [400, 1600]]
  await publicApi(page, { '/api/media/photo-groups': [{ id: 1, title: 'Gallery', photos: shapes.map((_, id) => ({ id, imageUrl: `/visual-photo/${id}.svg` })) }] })
  await page.route('**/visual-photo/*.svg', route => {
    const id = Number(new URL(route.request().url()).pathname.match(/(\d+)\.svg/)[1])
    const [w, h] = shapes[id]
    return route.fulfill({ contentType: 'image/svg+xml', body: `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}"><rect width="100%" height="100%" fill="${id % 2 ? '#cec9c3' : '#815d47'}"/></svg>` })
  })
  await page.goto('/media?section=photos')
  const images = page.locator('.photo-item img')
  await expect(images).toHaveCount(5)
  await images.evaluateAll(items => items.forEach(image => { image.loading = 'eager' }))
  await expect.poll(() => images.evaluateAll(items => items.every(img => img.complete && img.naturalWidth > 0))).toBe(true)
  await expect.poll(() => page.locator('.photo-item').evaluateAll(items => items.every(item => item.style.gridRowEnd.startsWith('span ')))).toBe(true)
  await expect(page.locator('.photos-grid')).toHaveScreenshot('gallery.png', { style: '.navbar, .legal-footer, .sns-links { visibility: hidden !important; }' })
})
