import { mount, flushPromises } from '@vue/test-utils'
import { beforeEach, afterEach, expect, it, vi } from 'vitest'
import { createPinia } from 'pinia'
import Login from '../../src/views/Login.vue'
const mocks = vi.hoisted(() => ({ login: vi.fn(), push: vi.fn() }))
vi.mock('../../src/services/authService', () => ({ login: mocks.login }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push: mocks.push }) }))
let wrapper
beforeEach(() => { vi.spyOn(console, 'error').mockImplementation(() => {}); mocks.login.mockReset(); mocks.push.mockReset(); wrapper = mount(Login, { global: { plugins: [createPinia()] } }) })
afterEach(() => wrapper.unmount())
it('retains credentials and restores submission after failure', async () => {
  mocks.login.mockRejectedValueOnce(new Error('Login failed')).mockResolvedValueOnce({})
  await wrapper.get('#username').setValue('admin'); await wrapper.get('#password').setValue('password')
  await wrapper.get('form').trigger('submit'); await flushPromises()
  expect(wrapper.get('.error-message').text()).toBe('Login failed')
  expect(wrapper.get('#username').element.value).toBe('admin')
  expect(wrapper.get('button').attributes()).not.toHaveProperty('disabled')
  await wrapper.get('form').trigger('submit'); await flushPromises()
  expect(mocks.push).toHaveBeenCalledWith('/admin')
})
it('only sends one request while a login is pending', async () => {
  let resolve; mocks.login.mockReturnValue(new Promise(r => { resolve = r }))
  await wrapper.get('form').trigger('submit'); await wrapper.get('form').trigger('submit')
  expect(mocks.login).toHaveBeenCalledTimes(1); expect(wrapper.get('button').attributes()).toHaveProperty('disabled')
  resolve({}); await flushPromises(); expect(mocks.push).toHaveBeenCalledTimes(1)
})
