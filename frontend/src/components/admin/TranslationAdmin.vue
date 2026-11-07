<template>
  <div class="translation-panel">
    <header class="panel-header">
      <h1>자동 번역</h1>
      <p>저장한 콘텐츠의 영어·독일어 번역을 확인하고 수정할 수 있습니다.</p>
    </header>
    <p v-if="loaded && !available" class="service-status" role="status">자동 번역이 비활성 상태입니다. 서버의 Gemini 키와 활성화 설정을 확인하세요.</p>
    <section class="filter-panel" aria-label="번역 작업 검색">
      <div class="tools">
      <label>콘텐츠 종류 <select v-model="kind" @change="page = 0; refresh()"><option value="">전체</option><option v-for="(label, key) in kinds" :key="key" :value="key">{{ label }}</option></select></label>
      <label>콘텐츠 ID <input v-model="contentId" type="number" min="1" :disabled="!kind" :placeholder="kind ? '전체' : '종류 선택 후 입력'" @change="page = 0; refresh()"></label>
      <button type="button" :disabled="busy" @click="refresh">새로고침</button>
      </div>
      <p v-if="loaded" class="page-summary">현재 페이지 · 대기·처리 <b>{{ pendingCount }}</b>건 · 실패 <b>{{ failedCount }}</b>건</p>
    </section>
    <p v-if="error" role="alert">{{ error }}</p>
    <p v-if="message" role="status">{{ message }}</p>
    <div class="translation-list">
      <article v-for="row in items" :key="row.id">
        <div class="row-heading"><h2>{{ kinds[row.kind] || row.kind }} #{{ row.contentId }} <small>{{ fields[row.field] || row.field }}</small></h2><span class="state" :class="`state-${row.state.toLowerCase()}`">{{ states[row.state] || row.state }}</span></div>
        <div class="source-block">
          <strong>원문</strong>
          <div class="source">
            <div v-for="entry in sourceEntries(row)" :key="entry.language">
              <strong v-if="entry.language" class="source-language">{{ entry.language }}</strong>
              <p>{{ entry.text }}</p>
            </div>
          </div>
        </div>
        <p v-if="row.errorCode" class="error">{{ errors[row.errorCode] || `번역 오류 (${row.errorCode})` }}</p>
        <template v-if="editing?.id === row.id">
          <div v-if="isRichText(row)" class="variants">
            <div><strong>English</strong><NewsletterEditor v-model="editing.en" editor-label="English 본문" :disabled="busy" /></div>
            <div><strong>Deutsch</strong><NewsletterEditor v-model="editing.de" editor-label="Deutsch 본문" :disabled="busy" /></div>
          </div>
          <div v-else class="variants">
            <label>English<textarea v-model="editing.en" rows="5" /></label>
            <label>Deutsch<textarea v-model="editing.de" rows="5" /></label>
          </div>
          <p>직접 바꾼 언어는 수동 번역으로 보존됩니다.</p>
          <div class="tools"><button class="primary" :disabled="busy" @click="save">번역 저장</button><button :disabled="busy" @click="editing = null">취소</button></div>
        </template>
        <template v-else>
          <div class="variants"><div><strong>English{{ row.manualEn ? ' · 직접 작성' : '' }}</strong><p>{{ displayText(row, row.en) || '—' }}</p></div><div><strong>Deutsch{{ row.manualDe ? ' · 직접 작성' : '' }}</strong><p>{{ displayText(row, row.de) || '—' }}</p></div></div>
          <div class="tools"><button v-if="['FAILED', 'DISABLED'].includes(row.state)" :disabled="busy || !available" @click="retry(row)">번역 재시도</button><button :disabled="busy || row.state === 'PROCESSING'" @click="editing = { ...row, en: row.en || '', de: row.de || '' }">번역 수정</button></div>
        </template>
      </article>
      <p v-if="loaded && !items.length && !error" class="empty">이 페이지에 번역 작업이 없습니다.</p>
      <div v-if="items.length || page > 0" class="tools pagination"><button :disabled="page === 0 || busy" @click="page--; refresh()">이전</button><span>{{ page + 1 }} 페이지</span><button :disabled="items.length < 100 || busy || !!(kind && contentId)" @click="page++; refresh()">다음</button></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onActivated, onDeactivated, onUnmounted, ref } from 'vue'
import api from '../../api/axios'
import NewsletterEditor from './common/NewsletterEditor.vue'
const items = ref([]), available = ref(false), loaded = ref(false), busy = ref(false), editing = ref(null)
const error = ref(''), message = ref(''), kind = ref(''), contentId = ref(''), page = ref(0)
const kinds = { projects: '프로젝트', concerts: '공연', news: '뉴스·뉴스레터', music: '음악', videos: '영상', photoGroups: '사진 그룹', photos: '사진', contacts: '연락처', contactInfo: '연락처 안내', downloads: '다운로드', equipment: '장비' }
const states = { PENDING: '번역 대기', PROCESSING: '번역 중', COMPLETED: '번역 완료', FAILED: '번역 실패', DISABLED: '비활성' }
const fields = { title: '제목', subtitle: '부제목', description: '설명', bodyEn: '본문' }
const pairedNewsFields = new Set(['title', 'bodyEn', 'eventWhenEn', 'eventLocationEn', 'ctaLabelEn'])
const isRichText = row => row.kind === 'news' && row.field === 'bodyEn'
function displayText(row, value) {
  if (!value || !isRichText(row)) return value
  const doc = new DOMParser().parseFromString(value, 'text/html')
  doc.querySelectorAll('script, style').forEach(node => node.remove())
  doc.body.querySelectorAll('p, div, h1, h2, h3, li, br').forEach(node => node.after('\n'))
  return doc.body.textContent.replace(/\n[\t \r]*\n+/g, '\n\n').trim()
}
function sourceEntries(row) {
  if (row.kind === 'news' && pairedNewsFields.has(row.field)) {
    try {
      const pair = JSON.parse(row.source)
      if (Array.isArray(pair) && pair.length === 2 && pair.every(value => typeof value === 'string')) {
        return pair.map((value, index) => ({
          language: index === 0 ? 'English' : 'Deutsch',
          text: displayText(row, value)
        })).filter(entry => entry.text)
      }
    } catch { /* Older source records may contain a single unwrapped value. */ }
  }
  return [{ language: '', text: displayText(row, row.source) || '—' }]
}
const errors = { GEMINI_HTTP_400: 'Gemini 요청 또는 API 키 설정을 확인하세요.', GEMINI_HTTP_401: 'Gemini API 키 인증에 실패했습니다.', GEMINI_HTTP_403: 'Gemini API 키 권한을 확인하세요.', GEMINI_HTTP_404: 'Gemini 모델 설정을 확인하세요.', GEMINI_HTTP_429: 'Gemini 할당량을 초과했습니다. 잠시 후 다시 시도하세요.', GEMINI_CONNECTION: 'Gemini 연결이 지연되거나 실패했습니다.', INVALID_RESPONSE: '번역 응답을 읽지 못했습니다.', INVALID_TRANSLATION: '번역 결과가 저장 조건에 맞지 않습니다.' }
const pendingCount = computed(() => items.value.filter(row => ['PENDING', 'PROCESSING'].includes(row.state)).length)
const failedCount = computed(() => items.value.filter(row => row.state === 'FAILED').length)
let timer, loading = false, requestId = 0
async function refresh() {
  const current = ++requestId
  loading = true
  try {
    const response = await api.get('/admin/translations', { params: { kind: kind.value || undefined, contentId: kind.value && contentId.value ? Number(contentId.value) : undefined, page: page.value }, handleErrorLocally: true })
    if (!Array.isArray(response.data?.data?.items)) throw new Error('Invalid translation response')
    if (current === requestId) { items.value = response.data.data.items; available.value = response.data.data.available; loaded.value = true; error.value = '' }
  } catch { if (current === requestId) error.value = '번역 상태를 불러오지 못했습니다. 새로고침해 주세요.' }
  finally { if (current === requestId) loading = false }
}
async function run(action, success) {
  busy.value = true; error.value = ''; message.value = ''
  try { await action(); message.value = success; await refresh() }
  catch (ex) { error.value = ex.response?.status === 409 ? '콘텐츠 또는 번역이 변경되었습니다. 새로고침 후 다시 수정하세요.' : ex.response?.data?.error?.message || '번역 작업을 완료하지 못했습니다.' }
  finally { busy.value = false }
}
function retry(row) { return run(() => api.post(`/admin/translations/${row.id}/retry`, {}, { handleErrorLocally: true }), '번역을 다시 대기열에 등록했습니다.') }
function save() { return run(async () => { await api.put(`/admin/translations/${editing.value.id}`, { version: editing.value.version, en: editing.value.en, de: editing.value.de }, { handleErrorLocally: true }); editing.value = null }, '번역을 저장했습니다.') }
onActivated(() => {
  if (!editing.value) refresh()
  timer = setInterval(() => {
    if (!document.hidden && !loading && !editing.value && !busy.value) refresh()
  }, 5000)
})
onDeactivated(() => clearInterval(timer))
onUnmounted(() => { requestId++; clearInterval(timer) })
</script>

<style scoped>
.translation-panel { color: var(--admin-ink); }
.panel-header h1 { margin: 0; font-size: clamp(1.65rem, 3vw, 2.25rem); }
.panel-header p { margin: .5rem 0 1.25rem; color: var(--admin-muted); }
.filter-panel, .translation-list article, .empty {
  background: var(--admin-surface);
  border: 1px solid var(--admin-line);
  border-radius: .75rem;
  padding: clamp(.9rem, 2vw, 1.25rem);
}
.tools, .row-heading { display: flex; flex-wrap: wrap; align-items: center; gap: .75rem; }
.filter-panel .tools { align-items: end; }
.filter-panel label { flex: 1 1 10rem; }
.page-summary { margin: .9rem 0 0; color: var(--admin-muted); }
.row-heading { justify-content: space-between; }
.row-heading h2 { margin: 0; font-size: 1rem; overflow-wrap: anywhere; }
.row-heading small { margin-inline-start: .35rem; color: var(--admin-muted); }
.translation-list article { margin-top: 1rem; }
.state { padding: .25rem .65rem; border-radius: 2rem; background: var(--admin-tint); font-size: .82rem; font-weight: 700; }
.state-completed { background: #e2f3eb; color: #225c43; }
.state-failed { background: #fce8e7; color: #922b35; }
.state-disabled { background: #f1f3f5; color: #475569; }
.source-block, .variants > div { padding: .8rem; border: 1px solid var(--admin-line); border-radius: .5rem; }
.source-block { margin-top: .9rem; background: var(--admin-canvas); }
.source, .variants p { white-space: pre-wrap; overflow-wrap: anywhere; max-height: 12rem; overflow-y: auto; }
.source, .variants p { margin: .4rem 0 0; }
.source p { margin: 0; }
.source > div + div { margin-top: .75rem; }
.source-language { display: block; margin-bottom: .25rem; color: var(--admin-muted); font-size: .85rem; }
.variants { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .75rem; margin-block: .75rem; }
label { display: block; font-weight: 600; }
button, input, select, textarea { font: inherit; padding: .55rem .7rem; min-height: 2.6rem; border: 1px solid var(--admin-line); border-radius: .4rem; color: inherit; background: white; }
input, select, textarea { display: block; width: 100%; box-sizing: border-box; margin-top: .35rem; }
button { cursor: pointer; }
button:hover:enabled { background: var(--admin-tint); }
button.primary { background: var(--admin-accent); border-color: var(--admin-accent); color: white; }
button.primary:hover:enabled { background: var(--admin-ink); }
button:disabled, input:disabled { opacity: .6; cursor: default; }
.service-status { padding: .8rem 1rem; background: #fff1d9; border-radius: .5rem; }
.error, [role=alert] { color: #922b35; }
.pagination { justify-content: center; margin-top: 1rem; }
@media(max-width: 640px) { .variants { grid-template-columns: 1fr; } }
</style>
