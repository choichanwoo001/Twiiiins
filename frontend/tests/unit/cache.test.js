import { afterEach, expect, it, vi } from 'vitest'
import { apiCache, createCacheKey, cachedApiCall } from '../../src/utils/apiCache'
afterEach(() => { apiCache.deletePattern('.*'); vi.useRealTimers() })
it('normalizes query key order and separates endpoints', () => {
  expect(createCacheKey('/a', { b: 2, a: 1 })).toBe(createCacheKey('/a', { a: 1, b: 2 }))
  expect(createCacheKey('/a')).not.toBe(createCacheKey('/b'))
  expect(createCacheKey('/a', { a: 1 })).not.toBe(createCacheKey('/a', { a: 2 }))
})
it('expires only after TTL and honors custom TTL', () => {
  vi.useFakeTimers(); vi.setSystemTime(0)
  apiCache.set('default', 1); apiCache.set('custom', 2, 100)
  vi.advanceTimersByTime(100); expect(apiCache.get('custom')).toBe(2)
  vi.advanceTimersByTime(1); expect(apiCache.get('custom')).toBeNull()
  expect(apiCache.get('default')).toBe(1)
  vi.advanceTimersByTime(300000); expect(apiCache.get('default')).toBeNull()
})
it('invalidates matching entries without deleting other resources', () => {
  apiCache.set('projects:1', [1]); apiCache.set('music:1', [2]); apiCache.deletePattern('^projects:')
  expect(apiCache.get('projects:1')).toBeNull(); expect(apiCache.get('music:1')).toEqual([2])
})
it.each([false, 0, '', [], { id: 1 }])('reuses cached %j without an extra request', async value => {
  const request = vi.fn().mockResolvedValue(value)
  expect(await cachedApiCall(request, 'resource')).toEqual(value)
  expect(await cachedApiCall(request, 'resource')).toEqual(value)
  expect(request).toHaveBeenCalledTimes(1)
})
it('does not cache rejected requests and allows retry', async () => {
  const request = vi.fn().mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce([1])
  await expect(cachedApiCall(request, 'retry')).rejects.toThrow('offline')
  expect(await cachedApiCall(request, 'retry')).toEqual([1])
})
