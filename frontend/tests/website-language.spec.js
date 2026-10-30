import { test, expect } from '@playwright/test'
import content from '../src/translations/content.json' with { type: 'json' }

const projectDescription = Object.keys(content.projects.descriptions)[0]
const review = Object.keys(content.projects['reviews.text'])[0]
const newsDescription = Object.keys(content.news.description)[0]
const newsHtml = Object.keys(content.news.body)[0]
const project = { id: 9, title: 'THE RESISTIBLE RISE OF ARTURO UI', subtitle: 'Collaboration with Salzburger Landestheater', premiereDate: '2025-02-22', location: 'Salzburg State Theatre', descriptions: [projectDescription], director: 'Alexandra Liedtke', urlSlug: 'arturo-ui', imageUrls: [], reviews: [{ text: review, source: 'Reichenhaller Tagblatt' }] }
async function mockWebsite(page) {
  await page.addInitScript(() => localStorage.setItem('twiiiins-cookie-preferences', JSON.stringify({ version: 1, media: false, savedAt: Date.now() })))
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname
    if (!path.startsWith('/api/')) return route.continue()
    let data = []
    if (path === '/api/projects') data = [project]
    if (path.includes('/projects/') && path !== '/api/projects') data = project
    if (path === '/api/concerts') data = [{ id: 34, date: '2026-12-19', name: 'TWIIIINS Concert', location: 'Märchenbazar München [DE]', fullLocation: 'Spcecial Concert', collaborationInfo: 'we are gonna be part of Gala show!', isPast: false }]
    if (path === '/api/media/contacts') data = [{ id: 1, name: 'Dowon Kim', role: 'Violin', email: 'music@dowonkim.com' }]
    if (path === '/api/media/download-files') data = [{ id: 20, name: 'CV, Tech Rider, Posters and Pictures', fileUrl: 'https://example.com/press' }]
    if (path === '/api/media/photo-groups') data = [{ id: 21, title: '27.08.2026 in Hamburg by Peter Jacob', photos: [] }]
    if (path === '/api/media/videos') data = [{ id: 8, title: 'About TWIIIINS', embedUrl: 'https://www.youtube.com/embed/7D3tv-8Fmlw' }]
    if (path === '/api/media/news') data = [
      { id: 1, date: '2023-12-01', title: 'Introducing Our Debut Single: Common Room', description: newsDescription, bodyEn: newsHtml, imageUrls: [] },
      { id: 2, date: '2026-10-20', title: 'A newly edited title', bodyEn: '<p>A newly edited body</p>', imageUrls: [] },
      { id: 3, date: '2026-10-20', title: 'Introducing Our Debut Single: Common Room', titleDe: 'Redaktioneller deutscher Titel', bodyEn: newsHtml, bodyDe: '<p>Redaktioneller deutscher Text</p>', imageUrls: [] },
    ]
    if (path === '/api/newsletter/status') data = { available: true }
    await route.fulfill({ json: { success: true, data } })
  })
}
async function selectLanguage(page, language) {
  await page.locator('.website-language summary').click()
  await page.getByRole('button', { name: language, exact: true }).click()
}

test('footer switch translates About and navigation, persists and supports keyboard', async ({ page, isMobile }, testInfo) => {
  await mockWebsite(page)
  await page.goto('/about')
  await expect(page.locator('.navbar .website-language')).toHaveCount(0)
  await expect(page.locator(isMobile ? '.legal-footer > :last-child' : '.sns-links > :last-child')).toHaveClass('website-language')
  await page.locator('.website-language summary').click()
  const trigger = await page.locator('.website-language summary').boundingBox()
  const options = await page.locator('.language-options').boundingBox()
  expect(options.y + options.height).toBeLessThan(trigger.y)
  expect(options.x).toBeGreaterThanOrEqual(0)
  await page.getByRole('button', { name: 'Deutsch', exact: true }).click()
  await expect(page.locator('.about h1')).toHaveText('ÜBER UNS')
  await expect(page.locator('.about-copy')).toContainText('TWIIIINS sind ein modernes Violinduo')
  await expect(page.locator('html')).toHaveAttribute('lang', 'de')
  await expect(page.locator('.about img').first()).toHaveAttribute('alt', /spielen ihre Geigen/)
  await expect(page.locator('.logo')).toHaveText('TWIIIINS')
  await page.reload()
  await expect(page.locator('.website-language summary')).toHaveText('Sprache · DE')
  if (isMobile) {
    await page.getByRole('button', { name: 'Menü öffnen' }).click()
    await expect(page.locator('.mobile-nav-links').getByRole('link', { name: 'KONTAKT', exact: true })).toBeVisible()
    await page.getByRole('button', { name: 'Menü schließen' }).click()
  } else await expect(page.locator('.desktop-nav').getByRole('link', { name: 'PROJEKTE' })).toBeVisible()
  expect(await page.locator('html').evaluate(el => el.scrollWidth > el.clientWidth)).toBe(false)
  await page.screenshot({ path: testInfo.outputPath('about-de.png'), fullPage: true })
  const summary = page.locator('.website-language summary')
  await summary.focus()
  await page.keyboard.press('Enter')
  await page.keyboard.press('Tab')
  await expect(page.getByRole('button', { name: 'English', exact: true })).toBeFocused()
  await page.keyboard.press('Enter')
  await expect(page.locator('.about h1')).toHaveText('ABOUT')
  await summary.click()
  await page.keyboard.press('Escape')
  await expect(page.locator('.website-language')).not.toHaveAttribute('open', '')
})

test('current content, media tabs and dates react to language without changing source data', async ({ page }, testInfo) => {
  await mockWebsite(page)
  await page.goto('/projects/arturo-ui')
  await selectLanguage(page, 'Deutsch')
  await expect(page.locator('.project-subtitle')).toHaveText('Zusammenarbeit mit Salzburger Landestheater')
  await expect(page.locator('.project-content')).toContainText('Wir wirkten als Komponisten')
  await expect(page.locator('.project-director')).toContainText('Regie: Alexandra Liedtke')
  await expect(page.locator('.project-date-location')).toContainText('22. Februar 2025')
  await selectLanguage(page, 'English')
  await expect(page.locator('blockquote')).toContainText('A successful, expressive')
  await page.goto('/concerts')
  await page.locator('.event-preview').click()
  await selectLanguage(page, 'Deutsch')
  await expect(page.locator('.date-main')).toHaveText('19. Dezember')
  await expect(page.locator('.event-name')).toHaveText('TWIIIINS Konzert')
  await expect(page.locator('.event-details')).toContainText('Wir sind bei der Galashow dabei!')
  await expect(page.locator('.event-details')).toContainText('Sonderkonzert')
  await page.goto('/contact')
  await expect(page.locator('.booking-agency .contact-role')).toHaveText('Konzertbuchung')
  await expect(page.locator('.booking-agency .contact-name')).toHaveText('Shaggy Schwarz')
  await expect(page.locator('.booking-agency h2')).toHaveText('BOOKING-AGENTUR')
  await expect(page.locator('#newsletter-heading')).toHaveText('IN VERBINDUNG BLEIBEN')
  await expect(page.locator('.general-contact h2')).toHaveText('KONTAKT AUFNEHMEN')
  await expect(page.locator('.general-contact > a')).toHaveText('contact@twiiiins.com')
  await expect(page.locator('.contact-details .contact-name')).toHaveText('Dowon Kim')
  await expect(page.locator('.contact-details .contact-role')).toHaveText('Geige')
  await expect(page.locator('.download-item')).toContainText('Lebenslauf, Technical Rider, Plakate und Bilder')
  await page.goto('/media?section=news&newsId=1')
  await expect(page.locator('.news-rich')).toContainText('Hallo zusammen')
  await expect(page.locator('.news-title').nth(1)).toHaveText('A newly edited title')
  await expect(page.locator('.news-title').nth(2)).toHaveText('Redaktioneller deutscher Titel')
  await page.locator('.news-preview').nth(1).click()
  await expect(page.locator('.news-rich').nth(1)).toHaveText('A newly edited body')
  await page.locator('.news-preview').nth(2).click()
  await expect(page.locator('.news-rich').nth(2)).toHaveText('Redaktioneller deutscher Text')
  await page.goto('/media?section=photos')
  await expect(page.locator('.photo-group-title')).toHaveText('27.08.2026 in Hamburg von Peter Jacob')
  await page.goto('/media?section=videos')
  await expect(page.locator('.video-title')).toHaveText('Über TWIIIINS')
  await expect(page.locator('.media-consent-placeholder')).toContainText('Einwilligung')
  await page.screenshot({ path: testInfo.outputPath('media-de.png'), fullPage: true })
  await page.goto('/shop')
  await expect(page.locator('.album-description')).toContainText('Hol dir dein physisches Exemplar')
  await page.goto('/')
  await expect(page.locator('.website-language summary')).toBeHidden()
  await expect(page.locator('.hero-description')).toContainText('modernes, dynamisches Violinduo')
  await page.locator('.home').evaluate(el => el.scrollTo({ top: el.clientHeight, behavior: 'instant' }))
  await expect(page.locator('.website-language summary')).toBeVisible()
  await selectLanguage(page, 'English')
  await expect(page.locator('.hero-description')).toContainText('modern, dynamic violin duo')
  await page.locator('.home').evaluate(el => el.scrollTo({ top: 0, behavior: 'instant' }))
  await expect(page.locator('.website-language summary')).toBeHidden()
  await page.goto('/newsletter/unsubscribe?token=test&lang=en')
  await page.getByRole('button', { name: 'Unsubscribe from newsletter', exact: true }).click()
  await expect(page.getByRole('status')).toContainText('You have been unsubscribed.')
  await selectLanguage(page, 'Deutsch')
  await expect(page.getByRole('status')).toContainText('Sie wurden abgemeldet.')
})

test('footer controls stay separate on narrow screens and empty-state copy is German', async ({ page }) => {
  await mockWebsite(page)
  await page.addInitScript(() => localStorage.setItem('twiiiins-website-language', 'de'))
  for (const width of [320, 800]) {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/about')
    const social = await page.locator('.sns-links').boundingBox()
    const legal = await page.locator('.legal-footer').boundingBox()
    expect(social.y + social.height).toBeLessThanOrEqual(legal.y)
    await page.locator('.website-language summary').click()
    const options = await page.locator('.language-options').boundingBox()
    expect(options.x).toBeGreaterThanOrEqual(0)
    expect(options.x + options.width).toBeLessThanOrEqual(width)
    await page.keyboard.press('Escape')
  }
  await page.route('**/api/media/videos', route => route.fulfill({ json: { success: true, data: [{ id: 8, title: 'About TWIIIINS', embedUrl: 'invalid' }] } }))
  await page.goto('/media?section=videos')
  await expect(page.locator('.empty-state')).toHaveText('Keine Videos gefunden.')
  await page.route('**/api/media/videos', route => route.fulfill({ json: { success: true, data: [] } }))
  await page.reload()
  await expect(page.locator('.empty-state')).toHaveText('Keine Videos gefunden.')
  await selectLanguage(page, 'English')
  await expect(page.locator('.empty-state')).toHaveText('No videos found.')
})
