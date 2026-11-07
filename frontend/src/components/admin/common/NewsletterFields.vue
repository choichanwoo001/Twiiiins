<template>
  <div class="newsletter-fields">
    <div class="row metadata">
      <label>게시 날짜<input v-model="form.date" type="date" required></label>
      <label>표시 순서<input v-model.number="form.displayOrder" type="number" min="0"></label>
    </div>

    <section class="field-section">
      <h3>제목과 본문</h3>
      <p class="field-hint">한 언어로만 작성해도 게시·발송할 수 있어요. 다른 언어가 비어 있으면 작성한 원문을 사용합니다.</p>
      <div class="row" role="group" aria-label="작성 언어">
        <button v-for="lang in ['en', 'de']" :key="lang" type="button" :aria-pressed="language === lang" @click="language = lang">
          {{ lang === 'en' ? 'English' : 'Deutsch' }}
        </button>
      </div>
      <label v-if="language === 'en'">영어 제목<input v-model="form.title" maxlength="255"></label>
      <label v-else>독일어 제목<input v-model="form.titleDe" maxlength="255"></label>
      <NewsletterEditor v-if="language === 'en'" key="en" v-model="form.bodyEn" :disabled="busy" />
      <NewsletterEditor v-else key="de" v-model="form.bodyDe" :disabled="busy" />
    </section>

    <section class="field-section">
      <h3>사진 <span class="optional">선택</span></h3>
      <p class="field-hint">첫 번째 사진이 대표 사진으로 표시됩니다.</p>
      <div v-if="form.imageUrls.length" class="photos">
        <div v-for="(url, index) in form.imageUrls" :key="url + index">
          <img :src="imageSource(url)" :alt="index === 0 ? '대표 사진' : `뉴스 사진 ${index + 1}`">
          <div>
            <button type="button" :disabled="index === 0 || busy" @click="moveImage(index, -1)">앞으로</button>
            <button type="button" :disabled="index === form.imageUrls.length - 1 || busy" @click="moveImage(index, 1)">뒤로</button>
            <button type="button" :disabled="busy" @click="form.imageUrls.splice(index, 1)">제거</button>
          </div>
        </div>
      </div>
      <label>사진 추가<input type="file" multiple accept="image/jpeg,image/png,image/webp,image/gif" :disabled="busy" @change="emit('upload', $event)"></label>
      <p v-if="uploading" class="field-hint" role="status">사진 업로드 중… 완료되면 미리보기에 반영됩니다.</p>
    </section>

    <section class="field-section">
      <h3>영상 링크 <span class="optional">선택</span></h3>
      <div v-for="(_, index) in form.videoUrls" :key="index" class="row video-row">
        <input v-model="form.videoUrls[index]" type="url" required placeholder="https://…" :aria-label="`영상 링크 ${index + 1}`">
        <button type="button" @click="form.videoUrls.splice(index, 1)">제거</button>
      </div>
      <button type="button" :disabled="form.videoUrls.length >= 20" @click="form.videoUrls.push('')">영상 링크 추가</button>
    </section>
  </div>
</template>
<script setup>
import NewsletterEditor from './NewsletterEditor.vue'
import { toAbsoluteUrl } from '../../../utils/commonHelpers'
const form = defineModel({ required: true })
const language = defineModel('language', { default: 'en' })
const props = defineProps({ busy: Boolean, uploading: Boolean, local: Boolean })
function imageSource(url) { return props.local && import.meta.env.DEV && import.meta.env.VITE_DUMMY_DATA === 'true' ? url : toAbsoluteUrl(url) }
const emit = defineEmits(['upload'])
function moveImage(index, direction) {
  const next = index + direction
  ;[form.value.imageUrls[index], form.value.imageUrls[next]] = [form.value.imageUrls[next], form.value.imageUrls[index]]
}
</script>
<style scoped>
.newsletter-fields { min-width: 0; }
.row { display: flex; gap: .65rem; flex-wrap: wrap; align-items: center; margin: .75rem 0; }
.metadata { margin: 0; }
.metadata label { flex: 1 1 8rem; margin: 0; }
.field-section { border-top: 1px solid var(--admin-line, #cbd9e4); margin-top: 1.25rem; padding-top: 1.15rem; }
.field-section h3 { margin: 0 0 .6rem; font-size: 1rem; line-height: 1.4; }
.field-hint { margin: 0 0 .75rem; font-size: .875rem; line-height: 1.6; color: var(--admin-muted, #475569); word-break: keep-all; overflow-wrap: anywhere; }
.optional { margin-inline-start: .25rem; color: var(--admin-muted, #475569); font-size: .8rem; font-weight: 500; }
label { display: block; margin: .75rem 0; font-size: .9rem; font-weight: 600; }
input { display: block; box-sizing: border-box; width: 100%; min-width: 0; padding: .65rem; border: 1px solid var(--admin-line, #bbb); border-radius: .35rem; margin-top: .35rem; font: inherit; font-weight: 400; }
button { padding: .5rem .7rem; border: 1px solid var(--admin-line, #bbb); border-radius: .35rem; background: var(--admin-surface, #fff); color: inherit; font: inherit; font-size: .875rem; cursor: pointer; }
button[aria-pressed=true] { background: var(--admin-tint, #e1edf6); border-color: var(--admin-accent, #245880); font-weight: 700; }
button:disabled { opacity: .5; cursor: default; }
.video-row input { flex: 1 1 10rem; margin: 0; }
.photos { display: flex; flex-wrap: wrap; gap: 1rem; }
.photos img { width: 130px; height: 100px; object-fit: cover; }
.photos button { font-size: .75rem; padding: .3rem; }
:focus-visible { outline:2px solid var(--admin-accent, #815d47);outline-offset:3px; }
</style>
