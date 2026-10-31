import { mount, flushPromises } from '@vue/test-utils'
import { reactive } from 'vue'
import { beforeEach, afterEach, expect, it, vi } from 'vitest'
import Detail from '../../src/views/ProjectDetail.vue'
const mocks = vi.hoisted(() => ({ read: vi.fn(), route: null }))
vi.mock('vue-router', () => ({ useRoute: () => mocks.route }))
vi.mock('../../src/services', () => ({ projectService: { getProjectBySlug: mocks.read } }))
vi.mock('../../src/utils/errorHandler', () => ({ logError: vi.fn() }))
let wrapper
beforeEach(() => { mocks.route = reactive({ params: { slug: 'first' } }); mocks.read.mockReset() })
afterEach(() => wrapper?.unmount())
function render() { wrapper = mount(Detail, { global: { stubs: { ImageGrid: true } } }) }
it('shows an error and can retry without leaving the page', async () => {
  mocks.read.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({ title: 'Recovered', imageUrls: [] })
  render(); await flushPromises(); expect(wrapper.get('[role=alert]').text()).toContain('Project unavailable')
  await wrapper.get('button').trigger('click'); await flushPromises()
  expect(wrapper.get('h1').text()).toBe('Recovered'); expect(wrapper.find('[role=alert]').exists()).toBe(false)
})
it('reacts to slug navigation and ignores a stale response', async () => {
  let resolve; mocks.read.mockReturnValueOnce(new Promise(r => { resolve = r })).mockResolvedValueOnce({ title: 'Second', imageUrls: [] })
  render(); mocks.route.params.slug = 'second'; await flushPromises()
  expect(wrapper.get('h1').text()).toBe('Second')
  resolve({ title: 'First', imageUrls: [] }); await flushPromises()
  expect(wrapper.get('h1').text()).toBe('Second')
})
