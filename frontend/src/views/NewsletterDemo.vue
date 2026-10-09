<template>
  <main class="newsletter-demo">
    <header><p class="eyebrow">TWIIIINS / NEWSLETTER STUDIO</p><h1>뉴스레터 작성 체험</h1><p>제목, 본문과 사진을 입력하면 발송 모습을 확인할 수 있습니다. 실제 저장·업로드·발송은 하지 않습니다.</p><div class="demo-actions"><button @click="fillExample">예시 내용 채우기</button><button @click="reset">초기화</button></div></header>
    <div class="demo-layout"><section class="demo-editor"><h2>콘텐츠 입력</h2><NewsletterFields v-model="form" v-model:language="language" local @upload="attach" /><p v-if="error" role="alert">{{ error }}</p></section><NewsletterPreview :form="form" :language="language" local /></div>
  </main>
</template>
<script setup>
import { ref, onBeforeUnmount } from 'vue'
import NewsletterFields from '../components/admin/common/NewsletterFields.vue'
import NewsletterPreview from '../components/admin/common/NewsletterPreview.vue'
const language = ref('en'), error = ref(''), urls = new Set()
function empty() { return { title:'',titleDe:'',bodyEn:'',bodyDe:'',date:new Date().toISOString().slice(0,10),displayOrder:0,imageUrls:[],videoUrls:[],eventWhenEn:'',eventWhenDe:'',eventLocationEn:'',eventLocationDe:'',ctaLabelEn:'',ctaLabelDe:'',ctaUrl:'' } }
const form = ref(empty())
function release() { urls.forEach(url => URL.revokeObjectURL(url)); urls.clear() }
function reset() { release(); form.value = empty(); language.value = 'en'; error.value = '' }
function fillExample() {
  reset()
  form.value = { ...empty(), title: 'SOUND IN MOTION', titleDe: 'KLANG IN BEWEGUNG',
    bodyEn: '<h2>A new conversation between sound and movement.</h2><p>Join TWIIIINS for an evening of contemporary music and performance. Discover new textures, shared rhythms and unexpected connections.</p><p>This is example content for the newsletter preview.</p>',
    bodyDe: '<h2>Ein neuer Dialog zwischen Klang und Bewegung.</h2><p>Erleben Sie mit TWIIIINS einen Abend voller zeitgenössischer Musik und Performance. Entdecken Sie neue Klangfarben, gemeinsame Rhythmen und überraschende Verbindungen.</p><p>Dies ist ein Beispiel für die Newsletter-Vorschau.</p>',
    eventWhenEn: 'Saturday, October 24 · 7:30 pm (example)', eventWhenDe: 'Samstag, 24. Oktober · 19:30 Uhr (Beispiel)',
    eventLocationEn: 'Performance Hall · Berlin (example)', eventLocationDe: 'Performance Hall · Berlin (Beispiel)',
    ctaLabelEn: 'Explore the performance', ctaLabelDe: 'Performance entdecken', ctaUrl: 'https://twiiiins.com/concerts',
    imageUrls: [examplePhoto] }
}
import examplePhoto from '../imgs/About/updated/seongsu-live.webp'
function attach(event) {
  error.value = ''
  const files = [...event.target.files]
  if (form.value.imageUrls.length + files.length > 50) { error.value = '사진은 최대 50장까지 첨부할 수 있습니다.'; event.target.value = ''; return }
  if (files.some(file => !['image/jpeg','image/png','image/webp','image/gif'].includes(file.type))) { error.value = '지원하는 이미지 파일을 선택하세요.'; event.target.value = ''; return }
  for (const file of files) { const url = URL.createObjectURL(file); urls.add(url); form.value.imageUrls.push(url) }
  event.target.value = ''
}
onBeforeUnmount(release)
</script>
<style scoped>
.newsletter-demo { max-width:1480px;margin:3rem auto;padding:0 2rem;color:#222; }header { max-width:800px;margin-bottom:2rem; }h1 { font-size:2rem;font-weight:500; }header p { line-height:1.7; }.eyebrow { font-size:.75rem;letter-spacing:2px;color:#666; }.demo-actions { display:flex;gap:.6rem; }button { padding:.7rem 1rem;background:#222;border:1px solid #222;color:white;cursor:pointer;border-radius:3px; }.demo-actions button:last-child { background:white;color:#222; }
.demo-layout { display:grid;grid-template-columns:minmax(0,1fr) minmax(0,680px);gap:2.5rem;align-items:start; }.demo-editor { min-width:0; }h2 { font-size:1.2rem;font-weight:500; }.demo-layout > :last-child { position:sticky;top:1rem; }
@media(max-width:1199px) { .demo-layout { grid-template-columns:minmax(0,1fr); }.demo-layout > :last-child { position:static; } }
@media(max-width:600px) { .newsletter-demo { padding:0 1rem;margin:2rem auto; }h1 { font-size:1.6rem; } }
</style>
