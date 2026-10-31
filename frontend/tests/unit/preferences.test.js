import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
beforeEach(() => { localStorage.clear(); vi.resetModules(); vi.useFakeTimers(); vi.setSystemTime(new Date('2026-10-31T00:00:00Z')) })
afterEach(() => vi.useRealTimers())
const load = () => import('../../src/composables/useWebsitePreferences')
it('defaults to English and external media disabled', async () => {
  const p = await load(); expect(p.websiteLanguage.value).toBe('en'); expect(p.mediaAllowed.value).toBe(false)
  expect(p.hasCookieChoice.value).toBe(false); expect(document.documentElement.lang).toBe('en')
})
it('persists language and consent, updates document language and permits revocation', async () => {
  const p = await load(); const copy = p.useWebsiteCopy({ label: 'Hello' }, { label: 'Hallo' })
  p.websiteLanguage.value = 'de'; await nextTick()
  expect(copy.value.label).toBe('Hallo'); expect(document.documentElement.lang).toBe('de')
  expect(localStorage.getItem('twiiiins-website-language')).toBe('de')
  p.cookieSettingsOpen.value = true; p.saveCookiePreferences(true)
  expect(p.cookieSettingsOpen.value).toBe(false); expect(p.hasCookieChoice.value).toBe(true)
  expect(p.mediaAllowed.value).toBe(true)
  vi.resetModules(); expect((await load()).mediaAllowed.value).toBe(true)
  p.saveCookiePreferences(false); expect(p.mediaAllowed.value).toBe(false)
})
it.each(['{broken', '{}', JSON.stringify({ version: 0, media: true, savedAt: Date.parse('2026-10-31') }),
  JSON.stringify({ version: 1, media: 'yes', savedAt: Date.parse('2026-10-31') }),
  JSON.stringify({ version: 1, media: true, savedAt: Date.parse('2026-11-01') }),
  JSON.stringify({ version: 1, media: true, savedAt: Date.parse('2026-01-01') })])('rejects invalid or expired consent %s', async stored => {
  localStorage.setItem('twiiiins-cookie-preferences', stored); expect((await load()).mediaAllowed.value).toBe(false)
})
it('keeps the visit usable when browser storage throws', async () => {
  vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => { throw new Error('disabled') })
  vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => { throw new Error('quota') })
  const p = await load(); p.websiteLanguage.value = 'de'; p.saveCookiePreferences(true); await nextTick()
  expect(p.websiteLanguage.value).toBe('de'); expect(p.mediaAllowed.value).toBe(true)
})
