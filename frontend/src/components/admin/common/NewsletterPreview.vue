<template>
  <aside class="mail-preview" aria-label="발송 메일 미리보기">
    <div class="preview-header">
      <h3>메일 미리보기</h3>
      <p class="subject"><span>메일 제목</span> {{ result.title || '제목을 입력하세요' }}</p>
      <div class="preview-controls">
        <div class="control-group" role="group" aria-label="미리보기 언어"><button v-for="lang in ['en', 'de']" :key="lang" type="button" :aria-label="`미리보기 ${lang === 'en' ? 'English' : 'Deutsch'}`" :aria-pressed="previewLanguage === lang" @click="previewLanguage = lang">{{ lang === 'en' ? 'English' : 'Deutsch' }}</button></div>
        <div class="control-group" role="group" aria-label="미리보기 크기"><button v-for="size in sizes" :key="size.width" type="button" :aria-pressed="width === size.width" @click="width = size.width">{{ size.label }}</button></div>
        <button class="expand-button" type="button" @click="openWide">넓게 보기</button>
      </div>
      <p v-if="error" class="preview-status" role="alert">미리보기를 갱신하지 못했습니다. 마지막 화면을 유지합니다. <button type="button" @click="refresh">다시 시도</button></p>
      <div class="preview-status" aria-live="polite">{{ loading ? '미리보기 갱신 중…' : '' }}</div>
    </div>
    <div class="preview-stage"><iframe title="발송 메일 실시간 미리보기" sandbox="" :style="{ width: width + 'px' }" :srcdoc="result.html" /></div>
    <p class="preview-note">구독 해지는 비활성 예시입니다. 발신 정보가 미설정이면 예시로 표시됩니다. 메일 앱에 따라 글꼴·모서리 표현이 다를 수 있습니다.</p>
    <dialog ref="wideDialog" class="wide-dialog" aria-label="메일 미리보기 넓게 보기" @close="expanded = false">
      <div class="wide-controls">
        <div class="control-group" role="group" aria-label="실제 미리보기 너비"><button v-for="size in sizes" :key="size.width" type="button" :aria-pressed="width === size.width" @click="width = size.width">{{ size.label }} {{ size.width }}px</button></div>
        <button type="button" autofocus @click="wideDialog.close()">닫기</button>
      </div>
      <div class="wide-stage" tabindex="0" role="region" aria-label="실제 너비 메일 미리보기">
        <iframe v-if="expanded" title="실제 너비 발송 메일 미리보기" sandbox="" :style="{ width: width + 'px' }" :srcdoc="result.html" />
      </div>
    </dialog>
  </aside>
</template>
<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { newsletterService } from '../../../services/newsletterService'
const props = defineProps({ form: { type: Object, required: true }, language: { type: String, default: 'en' }, local: Boolean })
const sizes = [{ label: '모바일', width: 375 }, { label: '데스크톱', width: 640 }]
const wideDialog = ref(null), expanded = ref(false)
const previewLanguage = ref(props.language), width = ref(640), result = ref({ title: '', html: '' }), loading = ref(false), error = ref(false)
function openWide() {
  wideDialog.value.showModal()
  expanded.value = true
}
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
.mail-preview { min-width:0;border:1px solid #ddd;border-radius:8px;overflow:hidden;background:var(--admin-surface, #f5f4f1);color:#222; }
.preview-header { padding: 1rem; }
h3 { margin: 0; font-size: 1.125rem; }
.subject { margin: .65rem 0 1rem; overflow-wrap: anywhere; font-size: .95rem; font-weight: 600; line-height: 1.5; }
.subject span { display: block; color: var(--admin-muted, #475569); font-size: .8rem; }
.preview-controls, .wide-controls, .control-group { display: flex; flex-wrap: wrap; gap: .4rem; align-items: center; }
.preview-controls, .wide-controls { gap: .65rem; }
.expand-button { margin-inline-start: auto; }
button { min-height: 2.25rem; border: 1px solid var(--admin-line, #bbb); background: white; color: #222; padding: .4rem .55rem; cursor: pointer; border-radius: .35rem; font-size: .85rem; }
button[aria-pressed=true] { background: var(--admin-tint, #e1edf6); color: var(--admin-ink, #132a41); border-color: var(--admin-accent, #245880); font-weight: 700; }
.preview-status, .preview-note { font-size: .85rem; line-height: 1.5; color: var(--admin-muted, #475569); }
.preview-status { margin: .6rem 0 0; }
.preview-status:empty { margin: 0; }
[role=alert] { color: #a12323; }
.preview-note { margin: .65rem 1rem; }
.preview-stage { padding: 12px; display: flex; justify-content: center; background: var(--admin-tint, #d9d8d4); }
iframe { display: block; border: 0; height: 720px; background: #FFFFFF; }
.preview-stage iframe { max-width: 100%; }
.wide-dialog { width: min(46rem, calc(100vw - 1rem)); box-sizing: border-box; border: 1px solid var(--admin-line, #bbb); border-radius: .65rem; padding: .75rem; }
.wide-dialog::backdrop { background: #10263b99; }
.wide-controls { justify-content: space-between; margin-bottom: .65rem; }
.wide-stage { overflow-x: auto; background: var(--admin-tint, #d9d8d4); }
.wide-stage iframe { margin-inline: auto; height: min(720px, 72dvh); }
:focus-visible { outline:2px solid var(--admin-accent, #815d47);outline-offset:3px; }
@media(max-width:600px) { .preview-stage { padding:0; } .preview-stage iframe { height:640px; } }
</style>
