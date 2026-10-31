import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, afterEach, expect, it } from 'vitest'
import { AxiosError } from 'axios'
import api from '../../src/api/axios'
import Status from '../../src/components/admin/common/AdminRequestStatus.vue'
import { adminRequestStatus as state, beginAdminRequest, finishAdminRequest, dismissAdminRequest, operationLabel } from '../../src/composables/useAdminRequestStatus'

let wrapper
beforeEach(() => {
  setActivePinia(createPinia()); state.items = []
  history.replaceState(null, '', '/admin')
  wrapper = mount(Status)
})
afterEach(() => { wrapper.unmount(); history.replaceState(null, '', '/') })
const resources = [
  ['/projects', '프로젝트'], ['/concerts', '공연'], ['/media/music', '음악'],
  ['/media/videos', '영상'], ['/media/equipment', '장비'], ['/media/contacts', '연락처'],
  ['/media/download-files', '다운로드'], ['/media/photos/groups', '사진 그룹'],
  ['/media/photos', '사진'], ['/media/news', '일반 News'], ['/admin/news', '뉴스레터'],
]
it.each(resources.flatMap(([url, name]) => ['post', 'put', 'delete'].map(method => [url, name, method])))('%s %s %s displays pending, failure, retry and success through the actual transport', async (url, name, method) => {
  let complete
  const action = method === 'post' ? url === '/media/photos' ? '업로드' : '등록' : method === 'put' ? '수정' : '삭제'
  const request = () => api.request({ url, method, handleErrorLocally: true, adapter: config => new Promise(resolve => { complete = (success) => {
    const response = { config, data: { success }, status: 200, statusText: 'OK', headers: {} }
    resolve(response)
  } }) })
  const first = request(); await flushPromises()
  expect(wrapper.get('[role=status]').text()).toBe(`${name} ${action} · 처리 중…`)
  expect(wrapper.get('[aria-busy]').attributes('aria-busy')).toBe('true')
  complete(false); await first; await flushPromises()
  expect(wrapper.get('[role=alert]').text()).toContain(`${name} ${action} · 실패`)
  const second = request(); await flushPromises(); complete(true); await second; await flushPromises()
  expect(wrapper.text()).toContain(`${name} ${action} · 성공`)
  expect(wrapper.findAll('[aria-busy=true]')).toHaveLength(0)
})
it.each([
  ['/api/admin/news/1/send', 'post', '뉴스레터 발송 등록'],
  ['/admin/news/1/publish', 'post', '뉴스레터 게시'], ['/admin/news/1/test', 'post', '뉴스레터 테스트 메일 발송'],
  ['/admin/newsletter/deliveries/1/retry', 'post', '뉴스레터 관리 재시도 등록'],
  ['/admin/newsletter/resume', 'post', '뉴스레터 관리 작업 재개'],
  ['/admin/newsletter/subscribers/1/exclude', 'post', '뉴스레터 관리 수신 대상 제외'],
  ['/admin/newsletter/settings', 'patch', '뉴스레터 관리 수정'], ['/admin/translations/1', 'put', '번역 수정'],
  ['/admin/translations/1/retry', 'post', '번역 재시도 등록'], ['/concerts/auto-move-past', 'put', '공연 이동'],
  ['/upload/image', 'post', '파일 업로드'], ['/projects/1/images', 'post', '프로젝트 업로드'],
  ['/media/photos/groups/1/photos', 'post', '사진 그룹 업로드'],
  ['/projects', 'get', null], ['/auth/login', 'post', null], ['/admin/newsletter/preview', 'post', null],
])('labels %s without claiming asynchronous delivery has finished', (url, method, label) => {
  expect(operationLabel({ url, method })).toBe(label)
})
it.each(['ECONNABORTED', 'ERR_NETWORK', 'ERR_CANCELED', 'ERR_BAD_RESPONSE'])('clears the pending indicator after %s', async code => {
  const promise = api.delete('/media/contacts/1', { handleErrorLocally: true, adapter: config => {
    throw new AxiosError('failure', code, config, undefined, code === 'ERR_BAD_RESPONSE' ? { data: { error: { message: '삭제할 수 없습니다.' } } } : undefined)
  } })
  await expect(promise).rejects.toBeInstanceOf(AxiosError); await flushPromises()
  expect(state.items[0].state).toBe(code === 'ERR_CANCELED' ? 'cancelled' : 'failure')
  expect(wrapper.findAll('[aria-busy=true]')).toHaveLength(0)
  if (code === 'ERR_BAD_RESPONSE') expect(wrapper.get('[role=alert]').text()).toContain('삭제할 수 없습니다.')
})
it('preserves concurrent pending operations when an earlier response arrives or history is trimmed', async () => {
  const earlier = { url: '/projects', method: 'post' }, later = { url: '/media/contacts', method: 'put' }
  beginAdminRequest(earlier); beginAdminRequest(later); dismissAdminRequest(later.adminOperationId)
  finishAdminRequest(earlier)
  for (const [url] of resources.slice(0, 8)) {
    const config = { url, method: 'post' }; beginAdminRequest(config); finishAdminRequest(config)
  }
  expect(state.items).toHaveLength(4)
  expect(state.items.find(item => item.id === later.adminOperationId).state).toBe('pending')
  finishAdminRequest(later); finishAdminRequest(later, { code: 'ERR_NETWORK' })
  expect(state.items.find(item => item.id === later.adminOperationId).state).toBe('success')
  await flushPromises(); await wrapper.find('button').trigger('click')
  expect(state.items).toHaveLength(2)
})
it('ignores public writes, automatic previews and unrelated response failures', () => {
  history.replaceState(null, '', '/contact'); beginAdminRequest({ url: '/projects', method: 'post' })
  history.replaceState(null, '', '/admin'); beginAdminRequest({ url: '/admin/newsletter/preview', method: 'post' })
  finishAdminRequest(undefined, new Error('unrelated'))
  expect(state.items).toEqual([])
})
