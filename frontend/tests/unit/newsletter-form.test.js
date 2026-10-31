import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import NewsletterSignup from '../../src/components/common/NewsletterSignup.vue'
import { websiteLanguage } from '../../src/composables/useWebsitePreferences'
const service = vi.hoisted(() => ({ available: vi.fn(), subscribe: vi.fn() }))
vi.mock('../../src/services/newsletterService', () => ({ newsletterService: service }))
let wrapper
beforeEach(() => { websiteLanguage.value = 'en'; service.available.mockResolvedValue({ available: true }); service.subscribe.mockReset() })
afterEach(() => wrapper?.unmount())
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
