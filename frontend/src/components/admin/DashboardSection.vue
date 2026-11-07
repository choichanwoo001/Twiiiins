<template>
  <section class="dashboard">
    <header class="dashboard-heading">
      <div><h1>대시보드</h1><p>구독자, 최근 발송과 예정 공연을 한곳에서 확인하세요.</p></div>
      <button type="button" :disabled="busy" @click="refresh">{{ busy ? '불러오는 중…' : '새로고침' }}</button>
    </header>

    <div class="dashboard-grid" :aria-busy="busy">
      <section class="metric-card">
        <h2>활성 구독자</h2>
        <template v-if="activeSubscribers">
          <p class="metric-value">{{ activeSubscribers.length }}<span>명</span></p>
          <p>영어 {{ activeSubscribers.filter(row => row.language === 'en').length }}명 · 독일어 {{ activeSubscribers.filter(row => row.language === 'de').length }}명</p>
        </template>
        <p v-else>{{ subscribersFailed ? '구독자 수를 불러오지 못했습니다.' : '구독자 집계 중…' }}</p>
        <button type="button" @click="$emit('navigate', 'newsletter', 'subscribers')">구독자 보기 →</button>
      </section>

      <section class="metric-card">
        <h2>최근 뉴스레터 발송</h2>
        <template v-if="latest">
          <p class="metric-title">{{ latest.titleEn || latest.titleDe }}</p>
          <p>{{ formatDate(latest.createdAt) }} · 대상 {{ latest.totalCount }}명</p>
          <dl v-if="deliverySummary" class="delivery-summary">
            <div><dt>SMTP 접수</dt><dd>{{ deliverySummary.accepted }}건</dd></div>
            <div><dt>대기·전송 중</dt><dd>{{ deliverySummary.pending }}건</dd></div>
            <div><dt>실패·확인 필요</dt><dd>{{ deliverySummary.attention }}건</dd></div>
            <div v-if="deliverySummary.skipped"><dt>발송 제외</dt><dd>{{ deliverySummary.skipped }}건</dd></div>
          </dl>
          <p v-else>{{ deliveriesFailed ? '발송 상세를 불러오지 못했습니다.' : '발송 결과 집계 중…' }}</p>
        </template>
        <p v-else>{{ mailingsFailed ? '발송 기록을 불러오지 못했습니다.' : mailings ? '등록된 발송이 없습니다.' : '발송 기록 확인 중…' }}</p>
        <button type="button" @click="$emit('navigate', 'newsletter', 'history')">발송 기록 보기 →</button>
      </section>

      <section class="metric-card">
        <h2>다음 공연</h2>
        <p v-if="concertStore.error">공연 일정을 불러오지 못했습니다.</p>
        <p v-else-if="concertStore.isLoading">공연 일정 확인 중…</p>
        <template v-else-if="nextConcert">
          <p class="metric-title">{{ nextConcert.name }}</p>
          <p>{{ nextConcert.date }}<template v-if="nextConcert.location"> · {{ nextConcert.location }}</template></p>
          <p>예정 공연 {{ upcoming.length }}건</p>
        </template>
        <p v-else>예정된 공연이 없습니다.</p>
        <button type="button" @click="$emit('navigate', 'concerts')">공연 관리 →</button>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { newsletterService } from '../../services/newsletterService'
import { useConcertStore } from '../../stores/useConcertStore'

defineEmits(['navigate'])
const concertStore = useConcertStore()
const subscribers = ref(null), mailings = ref(null), deliverySummary = ref(null)
const subscribersFailed = ref(false), mailingsFailed = ref(false), deliveriesFailed = ref(false), busy = ref(false)
const activeSubscribers = computed(() => subscribers.value?.filter(row => row.status === 'ACTIVE'))
const latest = computed(() => mailings.value?.reduce((newest, mailing) => {
  if (!newest) return mailing
  const difference = (Date.parse(mailing.createdAt) || 0) - (Date.parse(newest.createdAt) || 0)
  return (difference || mailing.id - newest.id) > 0 ? mailing : newest
}, null))
const upcoming = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return concertStore.upcomingConcerts.filter(concert => new Date(`${concert.date}T00:00:00`) >= today)
})
const nextConcert = computed(() => upcoming.value.reduce((next, concert) =>
  !next || concert.date < next.date ? concert : next, null))
const dateFormatter = new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' })
function formatDate(value) {
  const timestamp = Date.parse(value)
  return Number.isFinite(timestamp) ? dateFormatter.format(timestamp) : '날짜 미기록'
}
async function loadSubscribers() {
  try {
    const rows = await newsletterService.subscribers({ status: 'ACTIVE' })
    if (!Array.isArray(rows)) throw new Error('Invalid subscriber response')
    subscribers.value = rows
  } catch {
    subscribers.value = null
    subscribersFailed.value = true
  }
}
async function loadLatestMailing() {
  try {
    const rows = await newsletterService.mailings()
    if (!Array.isArray(rows)) throw new Error('Invalid mailing response')
    mailings.value = rows
  } catch {
    mailings.value = null
    mailingsFailed.value = true
    return
  }

  const mailing = latest.value
  if (!mailing) return
  try {
    const rows = await newsletterService.deliveries(mailing.id)
    if (!Array.isArray(rows)) throw new Error('Invalid delivery response')
    // Older delivery rows are removed and their counts are carried on the mailing.
    const summary = {
      accepted: mailing.acceptedCount ?? 0,
      pending: 0,
      attention: (mailing.failedCount ?? 0) + (mailing.unknownCount ?? 0),
      skipped: mailing.skippedCount ?? 0
    }
    for (const { status } of rows) {
      if (status === 'ACCEPTED') summary.accepted++
      if (status === 'WAITING' || status === 'SENDING') summary.pending++
      if (status === 'FAILED' || status === 'UNKNOWN') summary.attention++
      if (status === 'SKIPPED') summary.skipped++
    }
    deliverySummary.value = summary
  } catch {
    deliveriesFailed.value = true
  }
}
async function refresh() {
  busy.value = true
  subscribersFailed.value = false
  mailingsFailed.value = false
  deliveriesFailed.value = false
  deliverySummary.value = null
  try {
    await Promise.all([loadSubscribers(), loadLatestMailing(), concertStore.loadConcerts()])
  } finally {
    busy.value = false
  }
}
onMounted(refresh)
</script>

<style scoped>
.dashboard { max-width: 96rem; margin-inline: auto; padding: clamp(1rem, 3vw, 2.5rem); color: var(--admin-ink); }
.dashboard-heading { display: flex; justify-content: space-between; align-items: start; flex-wrap: wrap; gap: .85rem; margin-bottom: 1.25rem; }
h1 { margin: 0; font-size: clamp(1.65rem, 3vw, 2.25rem); }
h2 { margin: 0 0 .75rem; font-size: 1rem; }
p { margin: .35rem 0 .85rem; color: var(--admin-muted); line-height: 1.5; overflow-wrap: anywhere; }
.dashboard-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(min(100%, 17rem), 1fr)); gap: 1rem; }
.metric-card { display: flex; flex-direction: column; align-items: start; padding: 1.25rem; border: 1px solid var(--admin-line); border-radius: .75rem; background: var(--admin-surface); }
.metric-value { font-size: 2.3rem; line-height: 1.1; font-weight: 750; color: var(--admin-ink); }
.metric-value span { margin-inline-start: .2rem; font-size: 1rem; }
.metric-title { font-size: 1.15rem; font-weight: 700; color: var(--admin-ink); }
.delivery-summary { width: 100%; margin: 0 0 .85rem; padding: .65rem; box-sizing: border-box; border-radius: .4rem; background: var(--admin-canvas); font-size: .9rem; }
.delivery-summary div { display: flex; justify-content: space-between; gap: .5rem; padding: .2rem 0; }
.delivery-summary dd { margin: 0; font-weight: 700; }
button { min-height: 2.5rem; padding: .45rem .7rem; border: 1px solid var(--admin-line); border-radius: .4rem; background: var(--admin-surface); color: var(--admin-accent); font: inherit; font-weight: 650; cursor: pointer; }
button:hover:enabled { background: var(--admin-tint); }
button:disabled { opacity: .6; cursor: default; }
.metric-card button { margin-top: auto; }
</style>
