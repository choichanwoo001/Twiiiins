<template><div class="newsletter-fields">        <div class="row"><label>날짜<input v-model="form.date" type="date" required></label><label>표시 순서<input v-model.number="form.displayOrder" type="number" min="0"></label></div>
        <div class="row"><button v-for="lang in ['en', 'de']" :key="lang" type="button" :class="{ active: language === lang }" @click="language = lang">{{ lang === 'en' ? 'English' : 'Deutsch' }}</button></div>
        <label v-if="language === 'en'">영어 제목<input v-model="form.title" maxlength="255"></label><label v-else>독일어 제목<input v-model="form.titleDe" maxlength="255"></label>
        <NewsletterEditor v-if="language === 'en'" key="en" v-model="form.bodyEn" :disabled="busy" /><NewsletterEditor v-else key="de" v-model="form.bodyDe" :disabled="busy" />
        <p>날짜는 게시 날짜입니다.</p>
        <p>English 또는 Deutsch 중 한 언어의 제목과 본문만 작성해도 게시·발송할 수 있습니다. 다른 언어가 없으면 작성된 언어로 표시·발송하며 자동 번역하지 않습니다. 사진·영상 링크는 선택입니다.</p>
        <h4>사진 · 첫 번째가 대표 사진</h4>
        <div class="photos"><div v-for="(url, index) in form.imageUrls" :key="url + index"><img :src="imageSource(url)" alt="뉴스 사진"><div><button type="button" :disabled="index === 0 || busy" @click="moveImage(index, -1)">앞으로</button><button type="button" :disabled="index === form.imageUrls.length - 1 || busy" @click="moveImage(index, 1)">뒤로</button><button type="button" :disabled="busy" @click="form.imageUrls.splice(index, 1)">제거</button></div></div></div>
        <label>사진 추가<input type="file" multiple accept="image/jpeg,image/png,image/webp,image/gif" :disabled="busy" @change="emit('upload', $event)"></label>
        <p v-if="uploading" role="status">사진 업로드 중… 완료되면 미리보기에 반영됩니다.</p>
        <h4>영상 링크</h4><div v-for="(_, index) in form.videoUrls" :key="index" class="row"><input v-model="form.videoUrls[index]" type="url" required placeholder="https://…" :aria-label="`영상 링크 ${index + 1}`"><button type="button" @click="form.videoUrls.splice(index, 1)">제거</button></div>
        <button type="button" :disabled="form.videoUrls.length >= 20" @click="form.videoUrls.push('')">영상 링크 추가</button>
</div></template>
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
.newsletter-fields { min-width:0; } .row { display:flex;gap:.7rem;flex-wrap:wrap;align-items:center;margin:.8rem 0; }
label { display:block;margin:1rem 0;font-size:.9rem; } input { display:block;box-sizing:border-box;width:100%;min-width:0;max-width:100%;padding:.7rem;border:1px solid #bbb;border-radius:3px;margin-top:.4rem;font:inherit; } .row label { flex:1;min-width:130px; }
button { padding:.5rem .7rem;border:1px solid #bbb;background:#f8f7f5;cursor:pointer; } button.active { background:#ded4cc; } button:disabled { opacity:.5;cursor:default; }
p { font-size:.85rem;line-height:1.6;color:#666; } .photos { display:flex;flex-wrap:wrap;gap:1rem; }.photos img { width:130px;height:100px;object-fit:cover; }.photos button { font-size:.75rem;padding:.3rem; }
:focus-visible { outline:2px solid #815d47;outline-offset:3px; }
</style>
