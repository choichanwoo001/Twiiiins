import { test, expect, publicApi } from './support/fixtures'

for (const [width, height] of [[320, 568], [390, 844], [768, 1024], [1024, 768]]) {
  test(`home photo keeps both people centered and visible at ${width}px`, async ({ page }) => {
    await publicApi(page)
    await page.setViewportSize({ width, height })
    await page.goto('/')
    const image = page.locator('.hero-image img')
    await image.evaluate(img => img.decode())
    const frame = await page.locator('.hero-image picture').boundingBox()
    const photo = await image.boundingBox()

    // The full 1920x1080 photo's duo (including violins and shoes) spans x=255..980.
    // Project those landmarks into the visible portrait window, rather than
    // checking only the image element's alignment or a particular CSS offset.
    const left = photo.x + photo.width * 255 / 1920
    const right = photo.x + photo.width * 980 / 1920
    const center = (left + right) / 2
    expect(Math.abs(frame.x + frame.width / 2 - width / 2)).toBeLessThan(1)
    expect(Math.abs(center - (frame.x + frame.width / 2))).toBeLessThan(frame.width * 0.035)
    expect(left).toBeGreaterThan(frame.x)
    expect(right).toBeLessThan(frame.x + frame.width)
    expect(photo.width / photo.height).toBeCloseTo(1920 / 1080, 2)
    expect(await image.evaluate(img => img.currentSrc)).toMatch(/home-photo-(1280|1920)/)
  })
}

test('desktop home photo retains its full viewport cover layout', async ({ page }) => {
  await publicApi(page)
  await page.setViewportSize({ width: 1366, height: 900 })
  await page.goto('/')
  const image = page.locator('.hero-image img')
  await image.evaluate(img => img.decode())
  expect(await image.boundingBox()).toEqual({ x: 0, y: 0, width: 1366, height: 900 })
  await expect(image).toHaveCSS('object-fit', 'cover')
  await expect(image).toHaveCSS('object-position', '50% 50%')
  await expect(page.locator('.hero-description')).toHaveCSS('position', 'absolute')
})
