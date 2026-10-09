<template>
  <aside class="mail-preview" aria-label="발송 메일 미리보기">
    <div class="preview-heading"><div><p class="eyebrow">EMAIL PREVIEW</p><h3>발송 모습 미리보기</h3></div><span>{{ local ? '로컬 체험' : '저장 전 내용 포함' }}</span></div>
    <div class="preview-controls">
      <div role="group" aria-label="미리보기 언어"><button v-for="lang in ['en', 'de']" :key="lang" type="button" :aria-label="`미리보기 ${lang === 'en' ? 'English' : 'Deutsch'}`" :aria-pressed="previewLanguage === lang" @click="previewLanguage = lang">{{ lang === 'en' ? 'English' : 'Deutsch' }}</button></div>
      <div role="group" aria-label="미리보기 크기"><button type="button" :aria-pressed="width === 375" @click="width = 375">모바일 375</button><button type="button" :aria-pressed="width === 640" @click="width = 640">데스크톱 640</button></div>
    </div>
    <p class="subject">메일 제목: {{ result.title || '제목을 입력하세요' }}</p>
    <p v-if="error" role="alert">미리보기를 갱신하지 못했습니다. 마지막 화면을 유지합니다. <button type="button" @click="refresh">다시 시도</button></p>
    <p v-else aria-live="polite">{{ loading ? '미리보기 갱신 중…' : '입력한 내용이 자동으로 반영됩니다.' }}</p>
    <div class="preview-stage"><iframe title="발송 메일 실시간 미리보기" sandbox="" :style="{ width: width + 'px' }" :srcdoc="result.html" /></div>
    <p class="preview-note">구독 해지는 비활성 예시입니다. 발신 정보가 미설정이면 예시로 표시됩니다. 메일 앱에 따라 글꼴·모서리 표현이 다를 수 있습니다.</p>
  </aside>
</template>
<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { newsletterService } from '../../../services/newsletterService'
const props = defineProps({ form: { type: Object, required: true }, language: { type: String, default: 'en' }, local: Boolean })
const previewLanguage = ref(props.language), width = ref(640), result = ref({ title: '', html: '' }), loading = ref(false), error = ref(false)
let timer, revision = 0
watch(() => props.language, value => { previewLanguage.value = value })
function schedule() {
  clearTimeout(timer)
  const current = ++revision
  loading.value = true; error.value = false
  timer = setTimeout(() => render(current), 400)
}
async function render(current) {
  try {
    const data = JSON.parse(JSON.stringify(props.form))
    const language = previewLanguage.value
    const next = props.local && import.meta.env.DEV && import.meta.env.VITE_DUMMY_DATA === 'true'
      ? (await import('../../../dev/newsletterRenderer.js')).renderPreview(data, language)
      : await newsletterService.previewDraft(data, language)
    if (current !== revision) return
    if (typeof next?.html !== 'string') throw new Error('Invalid preview')
    result.value = next; error.value = false
  } catch { if (current === revision) error.value = true }
  finally { if (current === revision) loading.value = false }
}
function refresh() { clearTimeout(timer); loading.value = true; error.value = false; render(++revision) }
watch([() => props.form, previewLanguage], schedule, { deep: true, immediate: true })
onBeforeUnmount(() => { clearTimeout(timer); revision++ })
</script>
<style scoped>
.mail-preview { min-width:0;border:1px solid #ddd;border-radius:8px;overflow:hidden;background:#f5f4f1;color:#222; }
.preview-heading { display:flex;justify-content:space-between;align-items:center;gap:1rem;padding:1.2rem; }.eyebrow { margin:0 0 .4rem;font-size:.7rem;letter-spacing:2px;color:#666; }h3 { margin:0;font-size:1.1rem; }.preview-heading > span { font-size:.7rem;color:#666; }
.preview-controls { display:flex;justify-content:space-between;flex-wrap:wrap;gap:.6rem;padding:0 1.2rem; }.preview-controls > div { display:flex;gap:.25rem; }
button { border:1px solid #bbb;background:white;color:#222;padding:.5rem .6rem;cursor:pointer;border-radius:3px;font-size:.75rem; }button[aria-pressed=true] { background:#222;color:white;border-color:#222; }
.subject { overflow-wrap:anywhere;font-size:.85rem;font-weight:600;padding:0 1.2rem; }[role=status],[role=alert],.preview-note { font-size:.75rem;line-height:1.6;padding:0 1.2rem;color:#666; }[role=alert] { color:#a12323; }
.preview-stage { padding:12px;display:flex;justify-content:center;background:#d9d8d4; }iframe { display:block;border:0;max-width:100%;height:720px;background:#050505; }
:focus-visible { outline:2px solid #815d47;outline-offset:3px; }
@media(max-width:600px) { .preview-stage { padding:0; } iframe { height:640px; } }
</style>
