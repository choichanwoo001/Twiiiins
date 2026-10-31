import { reactive } from 'vue'

export const adminRequestStatus = reactive({ items: [] })
let sequence = 0
const resources = [
  [/^\/projects/, '프로젝트'], [/^\/concerts/, '공연'],
  [/^\/media\/music/, '음악'], [/^\/media\/videos/, '영상'],
  [/^\/media\/equipment/, '장비'], [/^\/media\/contacts/, '연락처'],
  [/^\/media\/download-files/, '다운로드'],
  [/^\/media\/(photos\/groups|photo-groups)/, '사진 그룹'], [/^\/media\/photos/, '사진'],
  [/^\/media\/news/, '일반 News'], [/^\/admin\/news(?:\/|$)/, '뉴스레터'],
  [/^\/admin\/translations/, '번역'], [/^\/admin\/newsletter/, '뉴스레터 관리'],
  [/^\/upload/, '파일'],
]

export function operationLabel(config) {
  const method = config.method?.toLowerCase()
  if (!['post', 'put', 'patch', 'delete'].includes(method)) return null
  const path = new URL(config.url, 'http://local').pathname.replace(/^\/api(?=\/)/, '')
  // Draft previews update automatically while typing and have their own status.
  if (path === '/admin/newsletter/preview') return null
  const resource = resources.find(([pattern]) => pattern.test(path))?.[1]
  if (!resource) return null
  let action = method === 'delete' ? '삭제' : ['put', 'patch'].includes(method) ? '수정' : '등록'
  if (/\/send$/.test(path)) action = '발송 등록'
  else if (/\/publish$/.test(path)) action = '게시'
  else if (/\/test$/.test(path)) action = '테스트 메일 발송'
  else if (/\/retry$/.test(path)) action = '재시도 등록'
  else if (/\/resume$/.test(path)) action = '작업 재개'
  else if (/\/exclude$/.test(path)) action = '수신 대상 제외'
  else if (/move|auto-move/.test(path)) action = '이동'
  else if (/^\/upload|\/images$|\/photos$/.test(path) && method === 'post') action = '업로드'
  return `${resource} ${action}`
}

function prune() {
  const finished = adminRequestStatus.items.filter(item => item.state !== 'pending').slice(-3)
  adminRequestStatus.items = adminRequestStatus.items.filter(item => item.state === 'pending' || finished.includes(item))
}
export function beginAdminRequest(config) {
  if (typeof window === 'undefined' || !window.location.pathname.startsWith('/admin')) return
  const label = operationLabel(config)
  if (!label) return
  const id = ++sequence
  config.adminOperationId = id
  adminRequestStatus.items = adminRequestStatus.items.filter(item => item.state === 'pending' || item.label !== label)
  adminRequestStatus.items.push({ id, label, state: 'pending', detail: '' })
  prune()
}
export function finishAdminRequest(config, error) {
  const item = adminRequestStatus.items.find(row => row.id === config?.adminOperationId)
  if (!item || item.state !== 'pending') return
  item.state = error ? error.code === 'ERR_CANCELED' ? 'cancelled' : 'failure' : 'success'
  item.detail = error ? error.response?.data?.error?.message || (error.code === 'ECONNABORTED'
    ? '응답 시간이 초과되었습니다. 저장 여부를 확인한 후 다시 시도해주세요.'
    : error.code === 'ERR_CANCELED' ? '요청이 취소되었습니다.' : '요청을 완료하지 못했습니다. 입력 내용을 확인하고 다시 시도해주세요.') : ''
  // Keep the most recently completed result, even if its request started earlier.
  adminRequestStatus.items = [...adminRequestStatus.items.filter(row => row.id !== item.id), item]
  prune()
}
export function dismissAdminRequest(id) {
  adminRequestStatus.items = adminRequestStatus.items.filter(item => item.id !== id || item.state === 'pending')
}
