import { test, expect, emptyApiResponse, publicApi } from './support/fixtures'
import content from '../src/translations/content.json' with { type: 'json' }

const projectDescription = Object.keys(content.projects.descriptions)[0]
const review = Object.keys(content.projects['reviews.text'])[0]
const newsDescription = Object.keys(content.news.description)[0]
const newsHtml = Object.keys(content.news.body)[0]
const project = { id: 9, title: 'THE RESISTIBLE RISE OF ARTURO UI', subtitle: 'Collaboration with Salzburger Landestheater', premiereDate: '2025-02-22', location: 'Salzburg State Theatre', descriptions: [projectDescription], director: 'Alexandra Liedtke', urlSlug: 'arturo-ui', imageUrls: [], reviews: [{ text: review, source: 'Reichenhaller Tagblatt' }] }
async function mockWebsite(page) {
  await page.addInitScript(() => { if (window !== window.top) return; localStorage.setItem('twiiiins-cookie-preferences', JSON.stringify({ version: 1, media: false, savedAt: Date.now() })) })
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname
    if (!path.startsWith('/api/')) return route.continue()
    let data
    if (path === '/api/newsletter/unsubscribe' && route.request().method() === 'POST') data = 'Unsubscribed'
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
    await route.fulfill({ json: { success: true, data: data === undefined ? emptyApiResponse(route.request()) : data } })
  })
}
async function selectLanguage(page, language) {
  await page.locator('.website-language summary').click()
  await page.getByRole('button', { name: language, exact: true }).click()
}

test('header switch translates About and navigation, persists and supports keyboard', async ({ page, isMobile }, testInfo) => {
  await mockWebsite(page)
  await page.goto('/about')
  await expect(page.locator('.navbar .website-language')).toBeVisible()
  await expect(page.locator('.legal-footer .website-language, .sns-links .website-language')).toHaveCount(0)
  const summary = page.locator('.website-language summary')
  const german = page.getByRole('button', { name: 'Deutsch', exact: true })
  await expect(summary).toHaveCSS('border-top-width', '0px')
  await expect(summary).toHaveCSS('background-color', 'rgba(0, 0, 0, 0)')
  if (!isMobile) {
    await expect(summary).toHaveCSS('font-size', await page.locator('.desktop-nav a').first().evaluate(el => getComputedStyle(el).fontSize))
    await summary.hover()
    await expect(german).toBeVisible()
    const optionBox = await german.boundingBox()
    await page.mouse.move(optionBox.x + optionBox.width / 2, optionBox.y + optionBox.height / 2, { steps: 12 })
    await expect(german).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(german).toBeHidden()
    await summary.hover()
    await expect(german).toBeVisible()
    await page.locator('.about h1').hover()
    await expect(german).toBeHidden()
  }
  await summary.click()
  const trigger = await summary.boundingBox()
  const options = await page.locator('.language-options').boundingBox()
  expect(options.y).toBeGreaterThan(trigger.y + trigger.height)
  expect(options.x).toBeGreaterThanOrEqual(0)
  await german.click()
  await expect(page.locator('.about h1')).toHaveText('ÜBER UNS')
  await expect(page.locator('.about-copy')).toContainText('TWIIIINS sind ein modernes Violinduo')
  await expect(page.locator('html')).toHaveAttribute('lang', 'de')
  await expect(page.locator('.about img').first()).toHaveAttribute('alt', /spielen ihre Geigen/)
  await expect(page.locator('.logo')).toHaveText('TWIIIINS')
  await page.reload()
  await expect(page.locator('.website-language summary')).toHaveAccessibleName('Sprache: Deutsch')
  if (isMobile) {
    await page.getByRole('button', { name: 'Menü öffnen' }).click()
    await expect(page.locator('.mobile-nav-links').getByRole('link', { name: 'KONTAKT', exact: true })).toBeVisible()
    await page.getByRole('button', { name: 'Menü schließen' }).click()
  } else await expect(page.locator('.desktop-nav').getByRole('link', { name: 'PROJEKTE' })).toBeVisible()
  expect(await page.locator('html').evaluate(el => el.scrollWidth > el.clientWidth)).toBe(false)
  await page.screenshot({ path: testInfo.outputPath('about-de.png'), fullPage: true })
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

test('project, concert dates and contact content follow language changes across navigation', async ({ page }) => {
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
  await expect(page.locator('.general-contact h2')).toHaveText('KONTAKT AUFNEHMEN')
  await expect(page.locator('.general-contact > a')).toHaveText('contact@twiiiins.com')
  await expect(page.locator('.contact-details .contact-name')).toHaveText('Dowon Kim')
  await expect(page.locator('.contact-details .contact-role')).toHaveText('Geige')
  await expect(page.locator('.download-item')).toContainText('Lebenslauf, Technical Rider, Plakate und Bilder')
})

test('media tabs and shop preserve German selection without changing edited source content', async ({ page }, testInfo) => {
  await mockWebsite(page)
  await page.goto('/about')
  await selectLanguage(page, 'Deutsch')
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
})

test('home and unsubscribe preserve language changes across navigation', async ({ page }) => {
  await mockWebsite(page)
  await page.goto('/about')
  await selectLanguage(page, 'Deutsch')
  await page.goto('/')
  const header = page.locator('.navbar')
  const languageSwitch = header.locator('.website-language summary')
  await expect(header).toBeVisible()
  await expect(languageSwitch).toBeInViewport()
  await expect(page.locator('.hero-description')).toContainText('modernes, dynamisches Violinduo')
  await page.locator('.home').evaluate(el => el.scrollTo({ top: el.clientHeight, behavior: 'instant' }))
  await expect(header).toBeHidden()
  await expect(languageSwitch).toBeHidden()
  await expect(page.locator('.sns-links')).toBeVisible()
  await page.locator('.home').evaluate(el => el.scrollTo({ top: 0, behavior: 'instant' }))
  await expect(header).toBeVisible()
  await expect(languageSwitch).toBeInViewport()
  await selectLanguage(page, 'English')
  await expect(page.locator('.hero-description')).toContainText('modern, dynamic violin duo')
  await page.goto('/newsletter/unsubscribe?token=test&lang=en')
  await page.getByRole('button', { name: 'Unsubscribe from newsletter', exact: true }).click()
  await expect(page.getByRole('status')).toContainText('You have been unsubscribed.')
  await selectLanguage(page, 'Deutsch')
  await expect(page.getByRole('status')).toContainText('Sie wurden abgemeldet.')
})

test('header dropdown fits narrow screens and empty-state copy is German', async ({ page }) => {
  await mockWebsite(page)
  await page.addInitScript(() => { if (window !== window.top) return; localStorage.setItem('twiiiins-website-language', 'de') })
  for (const width of [320, 769, 800, 1025, 1121]) {
    await page.setViewportSize({ width, height: 900 })
    await page.goto('/')
    await page.locator('.website-language summary').click()
    const options = await page.locator('.language-options').boundingBox()
    expect(options.x).toBeGreaterThanOrEqual(0)
    expect(options.x + options.width).toBeLessThanOrEqual(width)
    const logo = await page.locator('.logo').boundingBox()
    const actions = await page.locator('.header-actions').boundingBox()
    expect(logo.x + logo.width).toBeLessThanOrEqual(actions.x)
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

test.describe('News in a time zone west of UTC', () => {
  test.use({ timezoneId: 'America/Los_Angeles' })

  test('News has its own main menu link and shows date, image and translated title before expanding', async ({ page, isMobile }) => {
    await publicApi(page, {
      '/api/media/news': [
        { id: 1, date: '2026-04-26', title: 'Spring concert announcement', titleDe: 'Neuigkeiten zu unserem besonderen Frühlingskonzert', imageUrls: ['/Website_logo.jpg'], description: 'Concert details' },
        { id: 2, date: '2027-01-01', title: 'New year update', imageUrls: [] },
      ],
    })
    await page.goto('/media')
    const mediaTabs = page.locator('.media-nav .nav-item, .mobile-nav .mobile-nav-item').filter({ visible: true })
    await expect(mediaTabs).toHaveText(['MUSIC', 'VIDEOS', 'PHOTOS'])
    const mobileMenu = page.locator('.hamburger-button')
    const menu = page.locator('.desktop-nav:visible, .mobile-nav-links:visible')
    if (await mobileMenu.isVisible()) await mobileMenu.click()
    await expect(menu.locator('a[href="/news"] + a')).toHaveAttribute('href', '/concerts')
    await expect(menu.locator('.mobile-menu-item + .mobile-sub-menu + a[href="/news"], a[href="/media"] + a[href="/news"]')).toHaveCount(1)
    await menu.getByRole('link', { name: 'NEWS', exact: true }).click()
    await expect(page).toHaveURL(/\/news$/)
    const heading = page.getByRole('heading', { name: 'NEWS', level: 1 })
    const title = await heading.boundingBox()
    const signup = await page.locator('.newsletter-signup').boundingBox()
    await expect(heading).toHaveCSS('font-size', isMobile ? '40px' : '72px')
    await expect(heading).toHaveCSS('font-weight', '400')
    if (isMobile) {
      expect(title.x).toBe(signup.x)
      expect(title.y + title.height).toBeLessThan(signup.y)
    } else {
      expect(title.x + title.width).toBeLessThan(signup.x)
      expect(title.y).toBe(signup.y)
    }
    const firstStory = page.locator('.news-item').first()
    await expect(firstStory).toHaveCSS('border-top-width', '0px')
    const preview = firstStory.getByRole('button')
    await expect(preview).toHaveAttribute('aria-expanded', 'false')
    await expect(firstStory.locator('.news-thumbnail')).toBeVisible()
    await expect.poll(() => firstStory.locator('.news-thumbnail').evaluate(img => img.naturalWidth)).toBeGreaterThan(0)
    await expect(page.locator('.news-date')).toHaveText(['26.04.2026', '01.01.2027'])
    await expect(page.locator('.news-item').nth(1).locator('.news-thumbnail')).toHaveCount(0)
    await preview.press('Enter')
    await expect(preview).toHaveAttribute('aria-expanded', 'true')
    await expect(firstStory.locator('.news-details')).toContainText('Concert details')
    await selectLanguage(page, 'Deutsch')
    await expect(preview).toHaveAttribute('aria-expanded', 'true')
    await expect(page.locator('#newsletter-heading')).toHaveText('IN VERBINDUNG BLEIBEN')
    await expect(firstStory.locator('.news-title')).toHaveText('Neuigkeiten zu unserem besonderen Frühlingskonzert')
    await expect(page.locator('.news-date')).toHaveText(['26.04.2026', '01.01.2027'])
    await page.goto('/media')
    await expect(mediaTabs).toHaveText(['MUSIK', 'VIDEOS', 'FOTOS'])
    if (await mobileMenu.isVisible()) await mobileMenu.click()
    await menu.getByRole('link', { name: 'NEUIGKEITEN', exact: true }).click()
    await expect(page).toHaveURL(/\/news$/)
  })
})
