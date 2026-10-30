<template>
  <details class="translation-panel" @toggle="opened = $event.target.open">
    <summary>자동 번역 · {{ summary }}</summary>
    <p>콘텐츠는 먼저 저장되고 영어·독일어 번역이 이어집니다. 번역이 준비되기 전에는 원문이 표시됩니다.</p>
    <p v-if="!available" role="status">자동 번역이 비활성 상태입니다. 서버의 Gemini 키와 활성화 설정을 확인하세요.</p>
    <div class="tools">
      <label>콘텐츠 종류 <select v-model="kind" @change="page = 0; refresh()"><option value="">전체</option><option v-for="(label, key) in kinds" :key="key" :value="key">{{ label }}</option></select></label>
      <label>콘텐츠 ID <input v-model="contentId" type="number" min="1" placeholder="전체" @change="page = 0; refresh()"></label>
      <button type="button" :disabled="busy" @click="refresh">새로고침</button>
    </div>
    <p v-if="error" role="alert">{{ error }}</p>
    <p v-if="message" role="status">{{ message }}</p>
    <div v-if="opened" class="translation-list">
      <article v-for="row in items" :key="row.id">
        <div class="row-heading"><strong>{{ kinds[row.kind] || row.kind }} #{{ row.contentId }} · {{ row.field }}</strong><span>{{ states[row.state] || row.state }}</span></div>
        <p class="source">원문: {{ row.source }}</p>
        <p v-if="row.errorCode" class="error">{{ errors[row.errorCode] || `번역 오류 (${row.errorCode})` }}</p>
        <template v-if="editing?.id === row.id">
          <label>English<textarea v-model="editing.en" rows="5" /></label>
          <label>Deutsch<textarea v-model="editing.de" rows="5" /></label>
          <p>직접 바꾼 언어는 수동 번역으로 보존됩니다. HTML 본문은 태그를 포함해 편집할 수 있습니다.</p>
          <div class="tools"><button :disabled="busy" @click="save">번역 저장</button><button :disabled="busy" @click="editing = null">취소</button></div>
        </template>
        <template v-else>
          <div class="variants"><p><b>EN{{ row.manualEn ? ' · 직접 작성' : '' }}</b><br>{{ row.en || '번역 대기' }}</p><p><b>DE{{ row.manualDe ? ' · 직접 작성' : '' }}</b><br>{{ row.de || '번역 대기' }}</p></div>
          <div class="tools"><button v-if="['FAILED', 'DISABLED'].includes(row.state)" :disabled="busy || !available" @click="retry(row)">번역 재시도</button><button :disabled="busy || row.state === 'PROCESSING'" @click="editing = { ...row, en: row.en || '', de: row.de || '' }">번역 수정</button></div>
        </template>
      </article>
      <p v-if="!items.length">이 페이지에 번역 작업이 없습니다. 신규·수정 콘텐츠부터 표시됩니다.</p>
      <div class="tools"><button :disabled="page === 0 || busy" @click="page--; refresh()">이전</button><span>{{ page + 1 }} 페이지</span><button :disabled="items.length < 100 || busy || !!contentId" @click="page++; refresh()">다음</button></div>
    </div>
  </details>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import api from '../../api/axios'
const items = ref([]), available = ref(false), opened = ref(false), busy = ref(false), editing = ref(null)
const error = ref(''), message = ref(''), kind = ref(''), contentId = ref(''), page = ref(0)
const kinds = { projects: '프로젝트', concerts: '공연', news: '뉴스·뉴스레터', music: '음악', videos: '영상', photoGroups: '사진 그룹', photos: '사진', contacts: '연락처', contactInfo: '연락처 안내', downloads: '다운로드', equipment: '장비' }
const states = { PENDING: '번역 대기', PROCESSING: '번역 중', COMPLETED: '번역 완료', FAILED: '번역 실패', DISABLED: '비활성' }
const errors = { GEMINI_HTTP_400: 'Gemini 요청 또는 API 키 설정을 확인하세요.', GEMINI_HTTP_401: 'Gemini API 키 인증에 실패했습니다.', GEMINI_HTTP_403: 'Gemini API 키 권한을 확인하세요.', GEMINI_HTTP_404: 'Gemini 모델 설정을 확인하세요.', GEMINI_HTTP_429: 'Gemini 할당량을 초과했습니다. 잠시 후 다시 시도하세요.', GEMINI_CONNECTION: 'Gemini 연결이 지연되거나 실패했습니다.', INVALID_RESPONSE: '번역 응답을 읽지 못했습니다.', INVALID_TRANSLATION: '번역 결과가 저장 조건에 맞지 않습니다.' }
const summary = computed(() => {
  const pending = items.value.filter(r => ['PENDING', 'PROCESSING'].includes(r.state)).length
  const failed = items.value.filter(r => r.state === 'FAILED').length
  return !available.value ? '비활성' : `현재 페이지 대기·처리 ${pending}건 / 실패 ${failed}건`
})
let timer, loading = false, disposed = false
async function refresh() {
  if (loading || disposed) return
  loading = true
  try {
    const response = await api.get('/admin/translations', { params: { kind: kind.value || undefined, contentId: kind.value && contentId.value ? Number(contentId.value) : undefined, page: page.value }, handleErrorLocally: true })
    if (!Array.isArray(response.data?.data?.items)) throw new Error('Invalid translation response')
    if (!disposed) { items.value = response.data.data.items; available.value = response.data.data.available; error.value = '' }
  } catch { if (!disposed) error.value = '번역 상태를 불러오지 못했습니다. 새로고침해 주세요.' }
  finally { loading = false }
}
async function run(action, success) {
  busy.value = true; error.value = ''; message.value = ''
  try { await action(); message.value = success; await refresh() }
  catch (ex) { error.value = ex.response?.status === 409 ? '콘텐츠 또는 번역이 변경되었습니다. 새로고침 후 다시 수정하세요.' : ex.response?.data?.error?.message || '번역 작업을 완료하지 못했습니다.' }
  finally { busy.value = false }
}
function retry(row) { return run(() => api.post(`/admin/translations/${row.id}/retry`, {}, { handleErrorLocally: true }), '번역을 다시 대기열에 등록했습니다.') }
function save() { return run(async () => { await api.put(`/admin/translations/${editing.value.id}`, { version: editing.value.version, en: editing.value.en, de: editing.value.de }, { handleErrorLocally: true }); editing.value = null }, '번역을 저장했습니다.') }
onMounted(() => { refresh(); timer = setInterval(() => { if (!document.hidden && !editing.value && !busy.value) refresh() }, 5000) })
onUnmounted(() => { disposed = true; clearInterval(timer) })
</script>

<style scoped>
.translation-panel { margin: 0 0 1.5rem; padding: 1rem; background: #fff; border: 1px solid #cbd5e1; border-radius: .5rem; color: #1e293b; }
summary { cursor: pointer; font-weight: 600; }
.tools, .row-heading { display: flex; flex-wrap: wrap; align-items: center; gap: .75rem; }
.row-heading { justify-content: space-between; }
.translation-list article { border-top: 1px solid #cbd5e1; margin-top: 1rem; padding-top: 1rem; }
.source, .variants p { white-space: pre-wrap; overflow-wrap: anywhere; max-height: 12rem; overflow-y: auto; }
.variants { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; }
label { display: block; } textarea { display: block; width: 100%; box-sizing: border-box; } button, input, select, textarea { font: inherit; padding: .4rem; }
.error, [role=alert] { color: #9f1239; } button:disabled { opacity: .5; }
@media(max-width: 640px) { .variants { grid-template-columns: 1fr; } }
</style>
