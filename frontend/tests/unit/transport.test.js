import { beforeEach, afterEach, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { AxiosError } from 'axios'
import api from '../../src/api/axios'
import { useAppStore } from '../../src/stores/useAppStore'

beforeEach(() => { setActivePinia(createPinia()); localStorage.clear(); vi.useFakeTimers() })
afterEach(() => vi.useRealTimers())
function response(config, data = {}) { return { data, status: 200, statusText: 'OK', headers: {}, config } }
it('attaches the current bearer token and leaves JSON requests outside upload state', async () => {
  localStorage.setItem('token', 'test-session')
  const adapter = vi.fn(async config => response(config))
  await api.get('/test', { adapter })
  expect(adapter.mock.calls[0][0].headers.Authorization).toBe('Bearer test-session')
  expect(useAppStore().isUploading).toBe(false)
})
it('reports upload progress and clears state on success', async () => {
  const store = useAppStore(); const form = new FormData(); form.append('file', new File(['a'], 'a.png'))
  await api.post('/upload/image', form, { adapter: async config => {
    expect(store.isUploading).toBe(true); expect(config.timeout).toBe(120000)
    config.onUploadProgress({ loaded: 1, total: 2 }); expect(store.uploadProgress).toBe(50)
    return response(config)
  } })
  expect(store.isUploading).toBe(false); expect(store.uploadProgress).toBe(100)
  vi.advanceTimersByTime(500); expect(store.uploadProgress).toBe(0)
})
it.each([400, 401, 403, 404, 409, 429, 500, 502, 503, 504])('resets upload state and rejects HTTP %i', async status => {
  const store = useAppStore(); const form = new FormData(); form.append('file', new File(['a'], 'a.png'))
  await expect(api.post('/upload/image', form, { handleErrorLocally: true, adapter: async config => {
    config.onUploadProgress({ loaded: 1, total: 2 })
    throw new AxiosError('failure', 'ERR_BAD_RESPONSE', config, undefined, { ...response(config), status })
  } })).rejects.toBeInstanceOf(AxiosError)
  expect(store.isUploading).toBe(false); expect(store.uploadProgress).toBe(0)
})
it('rejects timeout and permits a fresh request', async () => {
  await expect(api.get('/test', { handleErrorLocally: true, adapter: config => Promise.reject(new AxiosError('timeout', 'ECONNABORTED', config)) })).rejects.toThrow('timeout')
  expect((await api.get('/test', { adapter: async config => response(config, { success: true }) })).data.success).toBe(true)
})
