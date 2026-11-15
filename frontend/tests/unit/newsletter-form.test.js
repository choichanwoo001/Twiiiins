import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import NewsletterSignup from '../../src/components/common/NewsletterSignup.vue'
import { websiteLanguage } from '../../src/composables/useWebsitePreferences'
const service = vi.hoisted(() => ({ available: vi.fn(), subscribe: vi.fn() }))
vi.mock('../../src/services/newsletterService', () => ({ newsletterService: service }))
let wrapper
beforeEach(() => { websiteLanguage.value = 'en'; service.available.mockResolvedValue({ available: true }); service.subscribe.mockReset() })
afterEach(() => {
  wrapper?.unmount()
  wrapper = undefined
  delete window.turnstile
  document.head.querySelector('script[src*="challenges.cloudflare.com"]')?.remove()
})
async function form() {
  wrapper = mount(NewsletterSignup, { global: { stubs: { RouterLink: { template: '<a><slot /></a>' } } } })
  await flushPromises(); await wrapper.get('input[type=email]').setValue('reader@example.com')
  await wrapper.get('input[type=checkbox]').setValue(true); return wrapper
}
it('blocks submission until availability is known and retries status failure', async () => {
  service.available.mockRejectedValueOnce(new Error('offline'))
  await form(); expect(wrapper.get('form button').attributes()).toHaveProperty('disabled')
  expect(wrapper.get('[role=alert]').text()).toContain('could not check')
  await wrapper.get('button[type=button]').trigger('click'); await flushPromises()
  expect(wrapper.get('form button').attributes()).not.toHaveProperty('disabled')
})
it('prevents duplicate submissions and clears fields only after success', async () => {
  let resolve; service.subscribe.mockReturnValue(new Promise(r => { resolve = r }))
  await form(); await wrapper.get('form').trigger('submit'); await wrapper.get('form').trigger('submit')
  expect(service.subscribe).toHaveBeenCalledTimes(1)
  expect(wrapper.get('input[type=email]').element.value).toBe('reader@example.com')
  expect(wrapper.get('form').attributes('aria-busy')).toBe('true')
  resolve({}); await flushPromises()
  expect(wrapper.get('input[type=email]').element.value).toBe('')
  expect(wrapper.get('input[type=checkbox]').element.checked).toBe(false)
  expect(wrapper.get('[role=status]').text()).toContain('Thank you')
})
it('retains input and selected language after failure, then retries successfully', async () => {
  service.subscribe.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({})
  await form(); await wrapper.get('select').setValue('de'); await wrapper.get('form').trigger('submit'); await flushPromises()
  expect(wrapper.get('input[type=email]').element.value).toBe('reader@example.com')
  expect(wrapper.get('select').element.value).toBe('de'); expect(wrapper.get('[role=status]').text()).toContain('could not process')
  await wrapper.get('form').trigger('submit'); await flushPromises()
  expect(service.subscribe).toHaveBeenLastCalledWith({ email: 'reader@example.com', language: 'de', consent: true })
})
it('blocks disabled service and requires explicit consent', async () => {
  service.available.mockResolvedValue({ available: false }); await form(); await wrapper.get('form').trigger('submit')
  expect(service.subscribe).not.toHaveBeenCalled(); expect(wrapper.get('[role=status]').text()).toContain('unavailable')
})
it('rejects malformed status and keeps input when SMTP becomes unavailable', async () => {
  service.available.mockResolvedValueOnce({ available: 'true' }); await form()
  expect(wrapper.find('[role=alert]').exists()).toBe(true)
  await wrapper.get('button[type=button]').trigger('click'); await flushPromises()
  service.subscribe.mockRejectedValueOnce({ response: { status: 503 } })
  await wrapper.get('form').trigger('submit'); await flushPromises()
  expect(wrapper.get('form button').attributes()).toHaveProperty('disabled')
  expect(wrapper.get('input[type=email]').element.value).toBe('reader@example.com')
})

it('retries loading verification after a script error or an incomplete load', async () => {
  service.available.mockResolvedValue({ available: true, turnstileSiteKey: 'public-site-key' })
  await form()
  const challengeScript = () => document.head.querySelector('script[src*="challenges.cloudflare.com"]')
  const retry = async () => {
    await wrapper.get('form button[type=button]').trigger('click')
    await flushPromises()
  }

  challengeScript().dispatchEvent(new Event('error'))
  await flushPromises()
  expect(wrapper.get('[role=alert]').text()).toContain('Verification could not be completed')
  expect(challengeScript()).toBeNull()

  await retry()
  challengeScript().dispatchEvent(new Event('load'))
  await flushPromises()
  expect(challengeScript()).toBeNull()

  await retry()
  window.turnstile = { render: vi.fn().mockReturnValue('widget'), remove: vi.fn() }
  challengeScript().dispatchEvent(new Event('load'))
  await flushPromises()
  expect(window.turnstile.render).toHaveBeenCalledOnce()
  expect(wrapper.find('[role=alert]').exists()).toBe(false)
})

it('submits fresh verification tokens and preserves the form when a token is rejected', async () => {
  service.available.mockResolvedValue({ available: true, turnstileSiteKey: 'public-site-key' })
  service.subscribe.mockRejectedValueOnce({ response: { status: 403 } }).mockResolvedValueOnce({})
  window.turnstile = { render: vi.fn().mockReturnValue('widget'), reset: vi.fn(), remove: vi.fn() }
  await form()
  const callbacks = window.turnstile.render.mock.calls[0][1]
  expect(callbacks).toMatchObject({ sitekey: 'public-site-key', action: 'newsletter_signup' })
  expect(wrapper.get('form button').attributes()).toHaveProperty('disabled')
  await wrapper.get('form').trigger('submit')
  expect(service.subscribe).not.toHaveBeenCalled()

  callbacks.callback('first-token')
  await wrapper.get('form').trigger('submit')
  await flushPromises()
  expect(service.subscribe).toHaveBeenLastCalledWith({
    email: 'reader@example.com', language: 'en', consent: true, turnstileToken: 'first-token'
  })
  expect(wrapper.get('[role=status]').text()).toContain('complete the new check')
  expect(wrapper.get('input[type=email]').element.value).toBe('reader@example.com')
  expect(window.turnstile.reset).toHaveBeenCalledWith('widget')
  expect(wrapper.get('form button').attributes()).toHaveProperty('disabled')

  callbacks.callback('fresh-token')
  await wrapper.get('form').trigger('submit')
  await flushPromises()
  expect(service.subscribe).toHaveBeenLastCalledWith({
    email: 'reader@example.com', language: 'en', consent: true, turnstileToken: 'fresh-token'
  })
  expect(wrapper.get('[role=status]').text()).toContain('Thank you')
})

it('blocks expired, timed-out and errored challenges and replaces the widget on retry', async () => {
  service.available.mockResolvedValue({ available: true, turnstileSiteKey: 'public-site-key' })
  const turnstile = { render: vi.fn().mockReturnValueOnce('old-widget').mockReturnValue('new-widget'), remove: vi.fn() }
  window.turnstile = turnstile
  await form()
  const callbacks = turnstile.render.mock.calls[0][1]

  callbacks.callback('expired-token')
  callbacks['expired-callback']()
  await flushPromises()
  expect(wrapper.get('form button').attributes()).toHaveProperty('disabled')
  expect(wrapper.find('[role=alert]').exists()).toBe(false)
  callbacks['timeout-callback']()
  await flushPromises()
  expect(wrapper.get('[role=alert]').text()).toContain('Verification could not be completed')

  await wrapper.get('form button[type=button]').trigger('click')
  await flushPromises()
  expect(turnstile.remove).toHaveBeenCalledWith('old-widget')
  expect(turnstile.render).toHaveBeenCalledTimes(2)
  expect(wrapper.find('[role=alert]').exists()).toBe(false)
  turnstile.render.mock.calls[1][1]['error-callback']()
  await flushPromises()
  expect(wrapper.find('[role=alert]').exists()).toBe(true)
  wrapper.unmount()
  wrapper = undefined
  expect(turnstile.remove).toHaveBeenLastCalledWith('new-widget')
})
