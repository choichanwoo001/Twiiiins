<template>
  <div class="newsletter-admin">
    <nav class="row" aria-label="뉴스레터 관리"><button v-for="tab in tabs" :key="tab.key" :class="{ active: section === tab.key }" @click="section = tab.key">{{ tab.label }}</button></nav>
    <div v-if="busy || message" class="operation-feedback" :class="operationState" :role="operationState === 'error' ? 'alert' : 'status'" :aria-live="operationState === 'error' ? 'assertive' : 'polite'" aria-atomic="true">
      <strong>{{ busy ? busyLabel : operationState === 'error' ? '처리 실패' : '처리 완료' }}</strong>
      <progress v-if="busy" aria-label="처리 진행 중"></progress>
      <p v-else>{{ message }}</p>
      <router-link v-if="!busy && operationState === 'success' && publishedId" :to="`/media?section=news&newsId=${publishedId}`" target="_blank" rel="noopener">게시글 확인 ↗</router-link>
      <button v-if="!busy" type="button" @click="message = ''" aria-label="처리 알림 닫기">닫기</button>
    </div>
    <p v-if="!settings.available" class="notice">SMTP 설정과 뉴스레터 활성화가 필요합니다. 초안 작성과 News 게시는 가능합니다. <button @click="section = 'settings'">설정 확인</button></p>
    <p v-if="settings.localMailboxUrl" class="notice">로컬에서는 환영·테스트·뉴스레터 메일이 외부 이메일 대신 로컬 메일함에 도착합니다. <a :href="settings.localMailboxUrl" target="_blank" rel="noopener">로컬 메일함 열기</a></p>
    <p v-if="settings.paused" class="notice">발송 중지: {{ settings.pauseReason }} <button :disabled="busy" @click="run(async () => { await service.resume(); await refresh() }, '발송을 재개했습니다.')">설정 확인 후 재개</button></p>

    <section v-if="section === 'news'">
      <div class="row"><h2>뉴스레터 작성</h2><button :disabled="busy" @click="reset">새 글 작성</button></div>
      <p class="list-help">서버에 저장된 뉴스레터 글 목록입니다. ‘초안’은 사이트에 공개되지 않으며, ‘게시’는 Media → News에 공개됩니다. 작성 중인 내용과 사진 변경은 저장해야 반영됩니다. 발송 기록이 있는 글은 삭제 대신 보관합니다.</p>
      <label>검색<input v-model="search" placeholder="제목 검색"></label>
      <div class="table-scroll"><table><thead><tr><th>날짜</th><th>제목</th><th>상태</th><th>작업</th></tr></thead><tbody>
        <tr v-for="item in filteredNews" :key="item.id"><td>{{ item.date }}</td><td>{{ item.title }}</td><td>{{ item.archived ? '보관' : item.status === 'DRAFT' ? '초안' : '게시' }}{{ mailingFor(item.id) ? ' · 발송 등록됨' : '' }}</td><td><button :disabled="busy || confirmOpen" @click="edit(item)">수정</button> <button :disabled="busy || confirmOpen || item.archived" @click="ask(mailingFor(item.id) ? '발송 기록이 있어 보관 처리합니다. 웹 보기 주소는 유지됩니다.' : '이 글을 서버에서 삭제할까요? 삭제한 초안은 새로고침해도 다시 나타나지 않습니다.', () => deleteItem(item))">{{ item.archived ? '보관됨' : mailingFor(item.id) ? '보관' : '삭제' }}</button></td></tr>
        <tr v-if="!filteredNews.length"><td colspan="4">저장된 뉴스레터 글이 없습니다.</td></tr>
      </tbody></table></div>
      <div class="compose-layout">
      <form class="compose" :aria-busy="busy" @submit.prevent="run(save, '서버에 저장했습니다.', '글 저장 중…')">
        <h3>{{ form.id ? '글 수정' : '새 글' }}</h3>
        <fieldset class="compose-fields" :disabled="busy || confirmOpen"><NewsletterFields v-model="form" v-model:language="language" :busy="busy || confirmOpen" :uploading="uploading" @upload="upload" /></fieldset>
        <section class="action-group"><h4>1. 초안 저장 · 화면 확인</h4><p>작성 내용을 저장하거나 메일 화면을 확인합니다. 구독자에게 보내지 않습니다.</p><div class="row"><button :disabled="busy">저장</button><button type="button" :disabled="busy" @click="preview">메일 미리보기</button></div></section>
        <section v-if="settings.testRecipients?.length" class="action-group"><h4>2. 테스트 메일</h4><p>선택한 테스트 주소 한 곳으로 현재 언어의 메일을 보냅니다.</p><div class="row"><label>테스트 수신 주소<select v-model="testEmail"><option v-for="email in settings.testRecipients" :key="email">{{ email }}</option></select></label><button type="button" :disabled="busy || !settings.available || !testEmail || settings.paused" @click="run(async () => { await save(); await service.test(form.id, language, testEmail) }, '테스트 메일을 대기열에 등록했습니다.')">{{ language.toUpperCase() }} 테스트 발송</button></div></section>
        <section class="action-group publish-group"><h4>3. 게시 · 구독자 발송</h4><p>News 게시는 사이트에만 공개합니다. 구독자 발송은 수신에 동의한 구독자에게 이메일도 보냅니다.</p><div class="row"><button type="button" :disabled="busy || confirmOpen || form.archived" @click="publishNews">News에만 게시</button><button type="button" class="send-button" :disabled="busy || confirmOpen || !settings.available || settings.paused || form.archived || !!mailingFor(form.id)" @click="prepareSend">게시 및 구독자 발송</button></div><p>게시·발송에는 English 또는 Deutsch 중 한 언어의 제목·본문이 필요합니다. 발송 등록 완료 후 메일별 진행 상황은 ‘발송 기록’에서 확인하세요.</p></section>
      </form>
      <NewsletterPreview :form="form" :language="language" />
      </div>
    </section>

    <section v-if="section === 'subscribers'">
      <div class="row"><h2>구독자</h2><button :disabled="busy" @click="run(refresh)">새로고침</button></div>
      <div class="verification-help"><h3>구독 등록 안내</h3><p>Contact에서 이메일 입력 + 수신 동의 → 바로 ‘구독 중’으로 등록 → 환영 메일 발송. 별도의 이메일 확인은 필요하지 않습니다.</p><p>환영 메일과 뉴스레터의 구독 해지 링크로 언제든 해지할 수 있습니다. 이전 방식의 인증 대기 주소는 기존 확인 링크를 이용하거나 Contact에서 다시 신청하면 됩니다.</p></div>
      <p>전체 신청자 {{ subscribers.length }}명 · 구독 중 {{ subscribers.filter(s => s.status === 'ACTIVE').length }}명 · 영어 {{ activeCount('en') }}명 · 독일어 {{ activeCount('de') }}명</p>
      <div class="row"><input v-model="subscriberSearch" type="search" placeholder="이메일 검색" aria-label="구독자 검색"><select v-model="subscriberLanguage" aria-label="수신 언어"><option value="">모든 언어</option><option value="en">English</option><option value="de">Deutsch</option></select><select v-model="subscriberStatus" aria-label="구독 상태"><option value="">모든 상태</option><option v-for="status in ['ACTIVE', 'PENDING', 'UNSUBSCRIBED', 'EXCLUDED']" :key="status" :value="status">{{ subscriberStatusLabel(status) }}</option></select></div>
      <p>검색 결과 {{ filteredSubscribers.length }}명</p>
      <div class="table-scroll"><table><thead><tr><th>이메일</th><th>언어</th><th>상태</th><th>신청일</th><th>작업</th></tr></thead><tbody><tr v-for="subscriber in filteredSubscribers" :key="subscriber.id"><td>{{ subscriber.email }}</td><td>{{ subscriber.language === 'de' ? 'Deutsch' : 'English' }}</td><td>{{ subscriberStatusLabel(subscriber.status) }}</td><td>{{ subscriberDate(subscriber.createdAt) }}</td><td><div class="subscriber-actions"><button :disabled="busy || subscriber.status === 'EXCLUDED'" @click="ask('이 주소를 발송 대상에서 제외할까요?', () => run(async () => { await service.exclude(subscriber.id); await refresh() }, '발송 대상에서 제외했습니다.'))">발송 제외</button></div></td></tr><tr v-if="!filteredSubscribers.length"><td colspan="5">조건에 맞는 구독자가 없습니다.</td></tr></tbody></table></div>
    </section>

    <section v-if="section === 'history'">
      <div class="row"><h2>발송 기록</h2><button :disabled="busy" @click="run(refreshHistory)">새로고침</button></div><p>‘SMTP 접수’는 메일 서버의 접수 상태입니다. 실제 받은편지함 도착을 뜻하지 않습니다.</p>
      <div class="table-scroll"><table><thead><tr><th>제목</th><th>전체</th><th>기록</th></tr></thead><tbody><tr v-for="mailing in mailings" :key="mailing.id"><td>{{ mailing.titleEn }}</td><td>{{ mailing.totalCount }}</td><td><button :disabled="busy" @click="run(async () => { selectedMailing = mailing; deliveryRows = await service.deliveries(mailing.id) })">결과 보기</button></td></tr></tbody></table></div>
      <template v-if="selectedMailing"><h3>{{ selectedMailing.titleEn }}</h3><p v-for="(count, state) in deliveryCounts" :key="state">{{ statusLabel(state) }}: {{ count }}</p>
        <div class="table-scroll"><table><thead><tr><th>이메일</th><th>언어</th><th>상태</th><th>오류</th><th>작업</th></tr></thead><tbody><tr v-for="delivery in deliveryRows" :key="delivery.id"><td>{{ delivery.email }}</td><td>{{ delivery.language }}</td><td>{{ statusLabel(delivery.status) }}</td><td>{{ delivery.errorCode }}</td><td><button v-if="delivery.status === 'FAILED'" :disabled="busy" @click="ask('명확히 실패한 메일을 다시 보낼까요?', () => run(async () => { await service.retry(delivery.id); await refreshHistory() }, '재시도를 등록했습니다.'))">재시도</button><span v-if="delivery.status === 'UNKNOWN'">SMTP 기록 수동 확인 필요</span></td></tr></tbody></table></div>
      </template>
    </section>
    <section v-if="section === 'settings'"><h2>발송 설정</h2><p>상태: {{ settings.available ? '사용 가능' : '비활성화 또는 설정 필요' }}</p><p>속도: 분당 {{ settings.perMinute }}건 · 하루 {{ settings.dailyLimit }}건 (UTC 기준, 환영·테스트 포함)</p><p>SMTP 계정, 비밀번호, 발신 정보와 한도는 서버 환경설정에서 관리합니다.</p>
      <div v-if="settings.configurationIssues?.length" class="configuration-issues"><h3>설정이 필요한 항목</h3><ul><li v-for="issue in settings.configurationIssues" :key="issue">{{ configurationIssueLabel(issue) }}</li></ul></div>
      <p v-else-if="settings.available">필수 설정이 입력되어 있습니다. 실제 메일 전달 여부는 허용된 테스트 수신 주소로 확인하세요.</p>
      <button :disabled="busy" @click="run(refresh)">설정 상태 새로고침</button>
    </section>
    <div v-if="previewOpen" class="overlay" @click.self="previewOpen = false"><section class="dialog" role="dialog" aria-modal="true" aria-label="뉴스레터 미리보기"><div class="row"><h3>{{ language.toUpperCase() }} 미리보기</h3><button @click="previewOpen = false">닫기</button></div><h4>{{ previewData.title }}</h4><iframe title="전체 메일 콘텐츠 미리보기" sandbox="" :srcdoc="previewData.html"></iframe><p>구독 해지는 비활성 예시입니다. 실제 발송 시 수신자별 링크가 적용됩니다.</p></section></div>
    <ConfirmDialog :is-visible="confirmOpen" title="확인" :message="confirmMessage" confirm-text="확인" cancel-text="취소" @confirm="confirmAction" @cancel="confirmOpen = false" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { newsletterService as service } from '../../services/newsletterService'
import NewsletterFields from './common/NewsletterFields.vue'
import NewsletterPreview from './common/NewsletterPreview.vue'
import ConfirmDialog from '../common/ConfirmDialog.vue'
import { completeNewsLanguages, hasNewsText } from '../../utils/newsLanguage'
const configurationIssueLabels = {
  NEWSLETTER_DISABLED: '뉴스레터 활성화 설정이 꺼져 있습니다. (NEWSLETTER_ENABLED)',
  SMTP_HOST_MISSING: 'SMTP 서버 주소를 설정하세요. (SMTP_HOST)',
  TOKEN_KEY_MISSING: '구독 해지 링크용 토큰 키를 설정하세요. (NEWSLETTER_TOKEN_KEY)',
  TOKEN_KEY_TOO_SHORT: '토큰 키는 최소 32자여야 합니다. 기존 키를 변경하면 발급된 링크가 무효화됩니다.',
  FROM_MISSING: '발신 주소를 설정하세요. (NEWSLETTER_FROM)',
  REPLY_TO_MISSING: '회신 주소를 설정하세요. (NEWSLETTER_REPLY_TO)',
  OPERATOR_MISSING: '메일 하단에 표시할 운영자 정보를 설정하세요. (NEWSLETTER_OPERATOR)',
  PUBLIC_URL_INVALID: '유효한 HTTPS 사이트 주소를 설정하세요. 로컬 개발에서는 localhost를 사용할 수 있습니다. (NEWSLETTER_PUBLIC_URL)',
  PER_MINUTE_INVALID: '분당 발송 한도는 0보다 커야 합니다. (NEWSLETTER_PER_MINUTE)',
  DAILY_LIMIT_INVALID: '하루 발송 한도는 0보다 커야 합니다. (NEWSLETTER_DAILY_LIMIT)'
}
const configurationIssueLabel = issue => configurationIssueLabels[issue] || issue
const uploading = ref(false)
const props = defineProps({ initialNewsId: { type: Number, default: null } })
const tabs = [{ key: 'news', label: '작성' }, { key: 'subscribers', label: '구독자' }, { key: 'history', label: '발송 기록' }, { key: 'settings', label: '설정' }]
const section = ref('news'), language = ref('en'), items = ref([]), settings = ref({}), subscribers = ref([]), mailings = ref([])
const search = ref(''), subscriberSearch = ref(''), subscriberLanguage = ref(''), subscriberStatus = ref(''), busy = ref(false), message = ref(''), testEmail = ref('')
const busyLabel = ref('불러오는 중…'), operationState = ref('idle'), publishedId = ref(null)
const selectedMailing = ref(null), deliveryRows = ref([]), previewOpen = ref(false), previewData = ref({}), confirmOpen = ref(false), confirmMessage = ref('')
let pendingAction = null
function empty() { const today = new Date(); const date = [today.getFullYear(), String(today.getMonth() + 1).padStart(2, '0'), String(today.getDate()).padStart(2, '0')].join('-'); return { title: '', titleDe: '', bodyEn: '', bodyDe: '', date, displayOrder: 0, imageUrls: [], videoUrls: [], eventWhenEn: '', eventWhenDe: '', eventLocationEn: '', eventLocationDe: '', ctaLabelEn: '', ctaLabelDe: '', ctaUrl: '' } }
const form = ref(empty())
const filteredNews = computed(() => items.value.filter(item => `${item.title} ${item.titleDe || ''}`.toLowerCase().includes(search.value.toLowerCase())))
const filteredSubscribers = computed(() => subscribers.value.filter(s => s.email.includes(subscriberSearch.value.toLowerCase()) && (!subscriberLanguage.value || s.language === subscriberLanguage.value) && (!subscriberStatus.value || s.status === subscriberStatus.value)))
const activeCount = lang => subscribers.value.filter(s => s.status === 'ACTIVE' && s.language === lang).length
const subscriberStatusLabel = status => ({ PENDING: '이전 인증 대기', ACTIVE: '구독 중', UNSUBSCRIBED: '구독 해지', EXCLUDED: '발송 제외' }[status] || status)
const subscriberDate = value => value ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
const deliveryCounts = computed(() => {
  const m = selectedMailing.value
  const counts = { ACCEPTED: m?.acceptedCount || 0, FAILED: m?.failedCount || 0, UNKNOWN: m?.unknownCount || 0, SKIPPED: m?.skippedCount || 0, WAITING: 0, SENDING: 0 }
  deliveryRows.value.forEach(d => { counts[d.status] = (counts[d.status] || 0) + 1 }); return counts
})
const statusLabel = state => ({ ACCEPTED: 'SMTP 접수', WAITING: '대기', SENDING: '발송 중', FAILED: '실패', UNKNOWN: '확인 필요', SKIPPED: '발송 제외' }[state] || state)
const mailingFor = id => mailings.value.find(m => m.newsId === id)
function reset() { form.value = empty(); language.value = 'en' }
function edit(item) { form.value = JSON.parse(JSON.stringify({ ...empty(), ...item, imageUrls: item.imageUrls || [], videoUrls: item.videoUrls || [] })); language.value = 'en' }
async function refresh() {
  const results = await Promise.all([service.news(), service.settings(), service.subscribers(), service.mailings()])
  ;[items.value, settings.value, subscribers.value, mailings.value] = results
  if (!testEmail.value) testEmail.value = settings.value.testRecipients?.[0] || ''
}
async function refreshHistory() { await refresh(); if (selectedMailing.value) { selectedMailing.value = mailings.value.find(m => m.id === selectedMailing.value.id); deliveryRows.value = await service.deliveries(selectedMailing.value.id) } }
async function refreshNews() { items.value = await service.news() }
async function run(action, success = '', pending = '처리 중…') {
  if (busy.value) return
  busy.value = true; message.value = ''; busyLabel.value = pending; operationState.value = 'pending'; publishedId.value = null
  try { await action(); message.value = success; operationState.value = 'success' }
  catch (error) { message.value = error.response?.data?.error?.message || error.message || '처리하지 못했습니다.'; operationState.value = 'error' }
  finally { busy.value = false }
}
async function save() {
  if (!form.value.date) throw new Error('날짜를 입력하세요.')
  if (!form.value.title?.trim() && !form.value.titleDe?.trim()) throw new Error('영어 또는 독일어 제목을 입력하세요.')
  form.value = await service.save({ ...form.value, videoUrls: form.value.videoUrls.filter(Boolean) })
  const savedItem = JSON.parse(JSON.stringify(form.value))
  items.value = items.value.some(item => item.id === savedItem.id)
    ? items.value.map(item => item.id === savedItem.id ? savedItem : item)
    : [...items.value, savedItem]
}
function validatePublication() {
  if (completeNewsLanguages(form.value).length) return
  if (form.value.title?.trim() || hasNewsText(form.value.bodyEn)) language.value = 'en'
  else if (form.value.titleDe?.trim() || hasNewsText(form.value.bodyDe)) language.value = 'de'
  throw new Error('English 또는 Deutsch 중 한 언어의 제목과 본문을 함께 입력하세요. 다른 언어는 비워 두어도 됩니다. 작성 내용은 그대로 유지됩니다.')
}
function publishNews() {
  run(async () => {
    validatePublication()
    await save()
    busyLabel.value = 'News에 게시 중…'
    form.value = await service.publish(form.value.id)
    publishedId.value = form.value.id
    busyLabel.value = '서버 게시 상태 확인 중…'
    await refreshNews()
    if (!items.value.some(item => item.id === form.value.id && item.status === 'PUBLISHED')) throw new Error('서버의 게시 상태를 확인하지 못했습니다. 목록을 새로고침하세요.')
  }, 'News에 게시했습니다. 아래 링크에서 실제 게시글을 확인하세요.', '게시할 내용 확인 중…')
}
function deleteItem(item) {
  run(async () => {
    await service.remove(item.id)
    busyLabel.value = '서버에서 삭제 결과 확인 중…'
    await refreshNews()
    const remaining = items.value.find(news => news.id === item.id)
    if (remaining && !remaining.archived) throw new Error('서버에 글이 남아 있어 삭제를 완료하지 못했습니다. 다시 시도하세요.')
    if (form.value.id === item.id) reset()
  }, mailingFor(item.id) ? '발송 기록이 있는 글을 보관했습니다. 웹 보기 주소는 유지됩니다.' : '서버에서 글을 삭제했습니다.', '글 삭제·보관 중…')
}
function upload(event) {
  const files = [...event.target.files]
  if (files.length) run(async () => {
    uploading.value = true
    try {
      if (form.value.imageUrls.length + files.length > 50) throw new Error('사진은 최대 50장까지 추가할 수 있습니다.')
      const urls = await service.upload(files)
      form.value.imageUrls.push(...urls)
    }
    finally { uploading.value = false; event.target.value = '' }
  }, '사진을 추가했습니다. 저장을 누르면 글에 반영됩니다.')
}
function preview() { run(async () => { previewData.value = await service.previewDraft(form.value, language.value); previewOpen.value = true }) }
function ask(text, action) { confirmMessage.value = text; pendingAction = action; confirmOpen.value = true }
function confirmAction() { confirmOpen.value = false; const action = pendingAction; pendingAction = null; action?.() }
function prepareSend() {
  run(async () => {
    validatePublication()
    await save()
    const { id, version } = form.value
    ask(`News에 게시하고 영어 ${activeCount('en')}명, 독일어 ${activeCount('de')}명에게 발송할까요? 발송 등록 후에는 수정한 내용이 이메일에 반영되지 않습니다.`, () => run(async () => {
      await service.send(id, version)
      publishedId.value = id
      busyLabel.value = '게시·발송 등록 결과 확인 중…'
      await refreshNews()
      mailings.value = await service.mailings()
      form.value = items.value.find(n => n.id === id) || form.value
      if (form.value.status !== 'PUBLISHED' || !mailingFor(id)) throw new Error('게시·발송 등록 상태를 확인하지 못했습니다. 발송 기록을 새로고침하세요.')
    }, 'News에 게시하고 발송 대기열에 등록했습니다. 메일별 전송 결과는 발송 기록에서 확인하세요.', '게시 및 구독자 발송 등록 중…'))
  }, '', '발송할 내용 저장 중…')
}
onMounted(() => run(async () => {
  await refresh()
  if (props.initialNewsId != null) {
    const item = items.value.find(n => n.id === props.initialNewsId)
    if (!item) throw new Error('뉴스레터를 찾을 수 없습니다. 목록을 새로고침하세요.')
    edit(item)
  }
}))
</script>
<style scoped>
.compose-fields { margin:0;padding:0;border:0;min-width:0; }
.list-help { color:#555;line-height:1.6;font-size:.9rem; }
.operation-feedback { position:fixed;right:1.25rem;bottom:1.25rem;z-index:3000;width:min(28rem,calc(100vw - 2.5rem));box-sizing:border-box;padding:1rem 1.25rem;background:#fff;border:2px solid #815d47;border-radius:8px;box-shadow:0 6px 28px #0003; }
.operation-feedback strong { display:block; }.operation-feedback p { margin:.5rem 0;line-height:1.6;white-space:pre-line; }.operation-feedback progress { display:block;width:100%;height:.6rem;margin-top:.75rem;accent-color:#815d47; }.operation-feedback.success { border-color:#287552; }.operation-feedback.error { border-color:#b42318; }.operation-feedback.error strong { color:#b42318; }.operation-feedback a { display:inline-block;margin-right:1rem;color:#287552;text-decoration:underline; }
.compose-layout { display:grid;grid-template-columns:minmax(0,1fr) minmax(0,680px);gap:2rem;align-items:start; }
.compose { min-width:0; } .compose-layout > :last-child { margin-top:2rem;position:sticky;top:1rem; }
@media (max-width:1199px) { .compose-layout { grid-template-columns:minmax(0,1fr); } .compose-layout > :last-child { position:static; } }

.action-group { margin-top:1.5rem;padding:1.25rem;border:1px solid #ddd;border-radius:8px;background:#fff; }
.action-group h4 { margin:0 0 .5rem;font-size:1rem; }.action-group p { margin:0 0 1rem;font-size:.85rem;line-height:1.6;color:#666; }.action-group .row { gap:1rem;margin:0; }.action-group label { margin:0;flex:1;min-width:160px; }.action-group select { width:100%;margin-top:.5rem; }.publish-group { border-color:#8d729b; }.send-button { background:#34203f;color:#fff;border-color:#34203f; }.verification-help { padding:1rem;background:#f1f5f3;border-radius:8px;margin:1rem 0; }.verification-help h3 { margin-top:0; }.verification-help p { line-height:1.7; }.subscriber-actions { display:flex;gap:.75rem;flex-wrap:wrap; }
@media (max-width:600px) { .action-group { padding:1rem; }.action-group .row > button { width:100%; }.action-group label { flex-basis:100%; } }
.table-scroll table { min-width: 40rem; }
.newsletter-admin { color: #222; }.row { display: flex; align-items: center; gap: .7rem; flex-wrap: wrap; margin: .8rem 0; }.active { background: #ded4cc; }button { border: 1px solid #bbb; background: #f8f7f5; padding: .55rem .8rem; border-radius: .2rem; cursor: pointer; color: #222; }button:disabled { opacity: .5; cursor: default; }label { display: block; margin: .8rem 0; }input:not([type=file]), select { display: block; padding: .65rem; border: 1px solid #bbb; border-radius: .2rem; max-width: 100%; box-sizing: border-box; }.compose > label > input { width: 100%; }.compose { border-top: 1px solid #ddd; margin-top: 2rem; padding-top: 1rem; }.table-scroll { overflow-x: auto; }table { border-collapse: collapse; width: 100%; font-size: .85rem; }th, td { text-align: left; padding: .7rem; border-bottom: 1px solid #ddd; overflow-wrap: anywhere; }.photos { display: flex; flex-wrap: wrap; gap: 1rem; }.photos img { width: 150px; height: 110px; object-fit: cover; }.photos button { font-size: .7rem; padding: .35rem; }.notice { background: #fff5dc; padding: 1rem; }.overlay { position: fixed; inset: 0; background: #0008; z-index: 1000; display: grid; place-items: center; padding: 1rem; }.dialog { background: white; padding: 1.5rem; max-width: 700px; width: 100%; max-height: 90vh; overflow: auto; }iframe { width: 100%; height: 55vh; border: 1px solid #ddd; }:focus-visible { outline: 2px solid #815d47; outline-offset: 2px; }
</style>
