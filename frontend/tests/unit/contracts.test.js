import { afterEach, describe, expect, it, vi } from 'vitest'
import { unwrapApiResponse } from '../../src/services/apiResponse'
import { isDropboxUrl } from '../../src/utils/downloadLinks'
import { hasNewsText, completeNewsLanguages, newsContentLanguage } from '../../src/utils/newsLanguage'
import * as payload from '../../src/services/payloadMappers'

afterEach(() => vi.useRealTimers())

describe('API response contract', () => {
  it.each([false, 0, '', null, [], { id: 1 }])('preserves wrapped data %j', value => {
    expect(unwrapApiResponse({ data: { success: true, data: value } })).toEqual(value)
  })
  it('supports unwrapped responses and a missing response fallback', () => {
    expect(unwrapApiResponse({ data: [1] })).toEqual([1])
    expect(unwrapApiResponse(undefined, [])).toEqual([])
  })
})

describe('download host boundaries', () => {
  it.each(['https://dropbox.com/s/a/file.pdf', 'https://www.dropbox.com/scl/fo/folder',
    'https://dl.dropboxusercontent.com/s/file.pdf'])('accepts %s', url => expect(isDropboxUrl(url)).toBe(true))
  it.each(['http://dropbox.com/s/a', 'https://dropbox.com.evil.test/s/a', 'https://evil.test/dropbox.com',
    'https://user@dropbox.com/s/a', 'https://user:pass@dropbox.com/s/a', 'https://dropbox.com:444/s/a',
    'https://dropbox.com/', 'javascript:alert(1)', '', '/uploads/a.pdf'])('rejects %s', url => expect(isDropboxUrl(url)).toBe(false))
})

describe('newsletter content language', () => {
  it.each(['', '<p> </p>', '<script>text</script>', '<style>text</style>', '<p>&nbsp;</p>'])('ignores empty markup %s', html => expect(hasNewsText(html)).toBe(false))
  it('selects a complete language without replacing source content', () => {
    const news = { title: ' ', bodyEn: '<p><br></p>', titleDe: 'Neuigkeiten', bodyDe: '<p>Hallo</p>' }
    expect(completeNewsLanguages(news)).toEqual(['de'])
    expect(newsContentLanguage(news, 'en')).toBe('de')
    expect(news.title).toBe(' ')
    expect(newsContentLanguage({}, 'en')).toBe('en')
    expect(completeNewsLanguages({ title: 'Hi', bodyEn: 'Hello', titleDe: 'Hallo', bodyDe: 'Welt' })).toEqual(['en', 'de'])
    expect(newsContentLanguage({ title: 'Hi', bodyEn: 'Hello', titleDe: 'Hallo', bodyDe: 'Welt' }, 'de')).toBe('de')
  })
  it.each(['en', 'de'])('requires both a nonblank title and body for %s', language => {
    const title = language === 'en' ? 'title' : 'titleDe', body = language === 'en' ? 'bodyEn' : 'bodyDe'
    expect(completeNewsLanguages({ [title]: 'Title' })).toEqual([])
    expect(completeNewsLanguages({ [body]: '<p>Body</p>' })).toEqual([])
    expect(completeNewsLanguages({ [title]: ' ', [body]: '<p>Body</p>' })).toEqual([])
  })
})

const cases = [
  ['Concert', { date: '2026-10-31', name: 'Concert', location: 'Seoul' }, ['date', 'name', 'location']],
  ['Project', { title: 'Project', premiereDate: '2026-10-31', urlSlug: 'project' }, ['title', 'premiereDate']],
  ['Music', { title: 'Track', artist: 'TWIIIINS', coverUrl: '/uploads/cover.png' }, ['title', 'artist', 'coverUrl']],
  ['Video', { title: 'Video', embedUrl: 'https://www.youtube.com/embed/test' }, ['title', 'embedUrl']],
  ['News', { title: 'News', date: '2026-10-31' }, ['title', 'date']],
  ['Equipment', { name: 'Violin' }, ['name']],
  ['PhotoGroup', { title: 'Photos' }, ['title']],
  ['Contact', { name: 'Booking', email: 'booking@example.com' }, ['name', 'email']],
  ['DownloadFile', { name: 'Rider', fileUrl: '/uploads/rider.pdf' }, ['name', 'fileUrl']],
]
describe.each(cases)('%s payload', (kind, valid, required) => {
  it('trims input and does not send UI-only fields', () => {
    const input = Object.fromEntries(Object.entries(valid).map(([k, v]) => [k, ` ${v} `]))
    expect(payload[`build${kind}CreatePayload`]({ ...input, isEditing: true })).toMatchObject(valid)
    expect(payload[`build${kind}CreatePayload`](input)).not.toHaveProperty('isEditing')
  })
  it.each(required)('rejects a blank required %s', key => {
    expect(() => payload[`build${kind}CreatePayload`]({ ...valid, [key]: ' ' })).toThrow()
  })
  it('supports an empty partial update', () => expect(payload[`build${kind}UpdatePayload`]({})).toEqual({}))
  it('retains supported update fields', () => expect(payload[`build${kind}UpdatePayload`](valid)).toMatchObject(valid))
})

it('preserves explicit clears, version, zero ordering and false values', () => {
  expect(payload.buildNewsUpdatePayload({ description: ' ', imageUrls: [], version: 0, displayOrder: 0 }))
    .toEqual({ description: '', imageUrls: [], version: 0, displayOrder: 0 })
  expect(payload.buildConcertUpdatePayload({ isPast: false, startTime: '25:99' })).toEqual({ isPast: false })
  expect(payload.buildConcertUpdatePayload({ startTime: '23:59' })).toEqual({ startTime: '23:59' })
  expect(payload.buildProjectUpdatePayload({ descriptions: [' Hello ', '', null], reviews: [{ text: '' }, { text: ' Great ', source: ' Press ' }] }))
    .toEqual({ descriptions: ['Hello'], reviews: [{ text: 'Great', source: 'Press' }] })
  expect(payload.sanitizeQueryParams({ q: ' ', limit: 0, active: false, tags: [' a ', '', null], absent: null }))
    .toEqual({ limit: '0', active: 'false', tags: ['a'] })
})

it.each(['Music', 'Equipment', 'Contact', 'DownloadFile', 'PhotoGroup', 'Project', 'Video'])('%s optional fields survive mapping', kind => {
  const valid = cases.find(([name]) => name === kind)[1]
  const data = { ...valid, displayOrder: '0', imageUrl: '/uploads/a.png', role: ' Booking ',
    linkUrl: 'https://example.com', downloadSource: 'upload', subtitle: 'Sub', location: 'Berlin',
    director: 'Director', thankYouText: 'Thanks', moreInfoUrl: 'https://example.com',
    descriptions: [], imageUrls: [], reviews: [{ text: 'Great' }], coverImageUrl: '/uploads/a.png' }
  expect(payload[`build${kind}CreatePayload`](data).displayOrder).toBe(0)
  expect(payload[`build${kind}UpdatePayload`](data).displayOrder).toBe(0)
})
