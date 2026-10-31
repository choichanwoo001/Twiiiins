import { mount, flushPromises } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import Concert from '../../src/components/admin/ConcertAdmin.vue'
import Project from '../../src/components/admin/ProjectAdmin.vue'
import Contact from '../../src/components/admin/ContactAdmin.vue'
import Downloads from '../../src/components/admin/DownloadFilesAdmin.vue'
import Music from '../../src/components/admin/tabs/MusicTab.vue'
import Video from '../../src/components/admin/tabs/VideoTab.vue'
import Equipment from '../../src/components/admin/tabs/EquipmentTab.vue'
import Photos from '../../src/components/admin/tabs/PhotoTab.vue'
import api from '../../src/api/axios'
import { useConcertStore } from '../../src/stores/useConcertStore'

const services = vi.hoisted(() => {
  const result = {}
  for (const [name, singular, plural] of [['concert','Concert','Concerts'],['project','Project','Projects'],
    ['contact','Contact','Contacts'],['downloadFile','DownloadFile','DownloadFiles'],['music','Music','Music'],
    ['video','Video','Videos'],['equipment','Equipment','Equipment'],['photo','PhotoGroup','PhotoGroups']]) {
    result[`${name}Service`] = Object.fromEntries([`getAll${plural}`, `create${singular}`, `update${singular}`,
      `delete${singular}`, `search${plural}`].map(method => [method, vi.fn()]))
  }
  result.uploadService = { uploadImage: vi.fn(), uploadFile: vi.fn() }
  return result
})
vi.mock('../../src/services', () => services)
vi.mock('../../src/api/axios', () => ({ default: { get: vi.fn().mockResolvedValue({ data: { data: [] } }) } }))
vi.mock('../../src/utils/errorHandler', () => ({ logError: vi.fn(), getErrorMessage: error => error.message }))
const alert = { props: ['isVisible'], emits: ['close'], template: '<button v-if="isVisible" data-alert @click="$emit(\'close\')">Dismiss alert</button>' }
const confirmation = { props: ['isVisible'], emits: ['confirm'], template: '<button v-if="isVisible" data-confirm @click="$emit(\'confirm\')">Confirm</button>' }
let wrapper
beforeEach(() => {
  for (const service of Object.values(services)) for (const fn of Object.values(service)) fn.mockReset().mockResolvedValue([])
  vi.spyOn(console, 'error').mockImplementation(() => {})
  vi.spyOn(window, 'alert').mockImplementation(() => {})
})
afterEach(() => wrapper?.unmount())
const cases = [
  ['Concert', Concert, 'concert', 'Concert'], ['Project', Project, 'project', 'Project'],
  ['Contact', Contact, 'contact', 'Contact'], ['Download', Downloads, 'downloadFile', 'DownloadFile'],
  ['Music', Music, 'music', 'Music'], ['Video', Video, 'video', 'Video'],
  ['Equipment', Equipment, 'equipment', 'Equipment'], ['Photos', Photos, 'photo', 'PhotoGroup'],
]
async function mountForm(component) {
  wrapper = mount(component, { global: { plugins: [createPinia()], stubs: {
    SearchFilters: true, DataTable: true, Modal: true, ConfirmDialog: confirmation, AlertDialog: alert,
  } } })
  await flushPromises()
  // In the application the parent Admin view loads this shared store.
  if (component === Concert) { await useConcertStore().loadConcerts(); await flushPromises() }
  const form = wrapper.find('form')
  for (const input of form.findAll('input:not([type=file]):not([type=checkbox])')) {
    const type = input.attributes('type')
    await input.setValue(type === 'date' ? '2026-10-31' : type === 'number' ? '0' : 'https://www.youtube.com/watch?v=abcdefghijk')
  }
  return form
}
describe.each(cases)('%s administrator form', (_, component, service, singular) => {
  const item = { id: 1, name: 'Existing', title: 'Existing', date: '2026-10-31', premiereDate: '2026-10-31',
    location: 'Berlin', urlSlug: 'existing', role: 'Booking', email: 'booking@example.com', artist: 'TWIIIINS',
    coverUrl: '/uploads/cover.png', fileUrl: 'https://www.dropbox.com/s/rider/rider.pdf', downloadSource: 'dropbox',
    embedUrl: 'https://www.youtube.com/embed/abcdefghijk', displayOrder: 0, isPast: false }
  function listMock() {
    if (service === 'project') return { mockResolvedValue: value => api.get.mockResolvedValue({ data: { data: value } }) }
    return Object.entries(services[`${service}Service`]).find(([key]) => key.startsWith('getAll'))[1]
  }
  async function act(action) {
    const table = wrapper.findComponent({ name: 'DataTable' })
    if (table.exists()) table.vm.$emit('action', action, item)
    else {
      const button = wrapper.findAll('tbody button').find(button => button.text() === (action === 'edit' ? '수정' : '삭제'))
      if (!button) throw new Error(wrapper.find('tbody').html())
      await button.trigger('click')
    }
    await flushPromises()
    if (wrapper.find('[data-confirm]').exists()) { await wrapper.get('[data-confirm]').trigger('click'); await flushPromises() }
  }
  async function dismiss() {
    if (wrapper.find('[data-alert]').exists()) { await wrapper.get('[data-alert]').trigger('click'); await flushPromises() }
  }
  it('keeps edited inputs after failure and retries the update without creating a new row', async () => {
    listMock().mockResolvedValue([item])
    const form = await mountForm(component)
    await act('edit')
    const before = form.findAll('input:not([type=file])').map(input => input.element.value)
    const update = services[`${service}Service`][`update${singular}`]
    update.mockRejectedValueOnce(new Error('update offline')).mockResolvedValueOnce(item)
    await form.trigger('submit'); await flushPromises(); await dismiss()
    expect(form.findAll('input:not([type=file])').map(input => input.element.value)).toEqual(before)
    expect(form.get('[type=submit]').attributes()).not.toHaveProperty('disabled')
    await form.trigger('submit'); await flushPromises(); await dismiss()
    expect(update).toHaveBeenCalledTimes(2)
    expect(update.mock.calls[0][0]).toBe(item.id)
    expect(services[`${service}Service`][`create${singular}`]).not.toHaveBeenCalled()
  })
  it('keeps the list after a rejected delete and permits a successful retry', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    listMock().mockResolvedValue([item])
    await mountForm(component)
    const remove = services[`${service}Service`][`delete${singular}`]
    remove.mockRejectedValueOnce(new Error('delete offline')).mockResolvedValueOnce(undefined)
    await act('delete'); await dismiss()
    expect(remove).toHaveBeenCalledTimes(1)
    const table = wrapper.findComponent({ name: 'DataTable' })
    if (table.exists()) expect(table.props('data')).toHaveLength(1)
    else expect(wrapper.findAll('tbody tr')).toHaveLength(1)
    listMock().mockResolvedValue([])
    await act('delete'); await dismiss()
    expect(remove).toHaveBeenCalledTimes(2)
    if (table.exists()) expect(table.props('data')).toHaveLength(0)
    else expect(wrapper.findAll('tbody tr')).toHaveLength(0)
  })
  it('prevents concurrent submits and restores the form after completion', async () => {
    const form = await mountForm(component)
    // File forms need a persisted URL; no upload is required to retry an already uploaded file.
    if (service === 'downloadFile' || service === 'music') {
      const crud = wrapper.findComponent({ name: 'CrudForm' })
      const key = service === 'music' ? 'coverUrl' : 'fileUrl'
      const current = crud.props('modelValue')
      crud.vm.$emit('update:modelValue', { ...current, [key]: service === 'music' ? '/uploads/test.png' : 'https://www.dropbox.com/scl/fi/test/rider.pdf' }); await flushPromises()
    }
    let resolve
    const create = services[`${service}Service`][`create${singular}`]
    create.mockReturnValue(new Promise(r => { resolve = r }))
    await form.trigger('submit'); await form.trigger('submit'); await flushPromises()
    expect(create).toHaveBeenCalledTimes(1)
    expect(form.find('[type=submit]').attributes()).toHaveProperty('disabled')
    expect(form.find('[type=submit]').text()).toContain('저장 중')
    resolve({ id: 1 }); await flushPromises()
    if (wrapper.find('[data-alert]').exists()) { await wrapper.get('[data-alert]').trigger('click'); await flushPromises() }
    expect(form.find('[type=submit]').attributes()).not.toHaveProperty('disabled')
  })
  it('retains input after a rejected save and allows a new attempt', async () => {
    const form = await mountForm(component)
    if (service === 'downloadFile' || service === 'music') {
      const crud = wrapper.findComponent({ name: 'CrudForm' })
      crud.vm.$emit('update:modelValue', { ...crud.props('modelValue'), [service === 'music' ? 'coverUrl' : 'fileUrl']: service === 'music' ? '/uploads/test.png' : 'https://www.dropbox.com/scl/fi/test/rider.pdf' }); await flushPromises()
    }
    const before = form.findAll('input:not([type=file])').map(input => input.element.value)
    const create = services[`${service}Service`][`create${singular}`]
    create.mockRejectedValueOnce(new Error('offline')).mockResolvedValueOnce({ id: 1 })
    await form.trigger('submit'); await flushPromises()
    if (service === 'contact') expect(wrapper.get('[role=alert]').text()).toBe('offline')
    if (wrapper.find('[data-alert]').exists()) { await wrapper.get('[data-alert]').trigger('click'); await flushPromises() }
    expect(form.findAll('input:not([type=file])').map(input => input.element.value)).toEqual(before)
    expect(form.find('[type=submit]').attributes()).not.toHaveProperty('disabled')
    await form.trigger('submit'); await flushPromises(); expect(create).toHaveBeenCalledTimes(2)
    if (wrapper.find('[data-alert]').exists()) { await wrapper.get('[data-alert]').trigger('click'); await flushPromises() }
  })
})
