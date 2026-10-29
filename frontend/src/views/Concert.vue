<template>
  <div class="concert">
    <!-- 좌측 고정 타이틀 -->
    <aside class="concert-title">
      <h1>{{ t('CONCERT') }}</h1>
    </aside>

    <!-- 통합된 이벤트 섹션 -->
    <main class="events-container">
      <!-- Upcoming Events -->
      <ConcertSection 
        :title="t('Upcoming Events')"
        :events="upcomingEvents" 
        @toggle="toggleEvent" 
      />

      <!-- Past Events -->
      <ConcertSection 
        :title="t('Past Events')"
        :events="pastEvents" 
        :is-past="true"
        @toggle="togglePastEvent" 
      />
    </main>
  </div>
</template>

<script setup>
import { t, websiteLocale, translateContent } from '../composables/useWebsiteTranslation'
import { ref, computed, onMounted } from 'vue'
import { concertService } from '../services'
import ConcertSection from '../components/ConcertSection.vue'
import { logError } from '../utils/errorHandler'

const concerts = ref([])
const expandedIds = ref(new Set())
const localizedConcerts = computed(() => concerts.value.map(concert => {
  const date = new Date(`${concert.date}T12:00:00`)
  return {
    ...concert,
    dateMain: new Intl.DateTimeFormat(websiteLocale.value, { month: 'long', day: 'numeric' }).format(date),
    dateYear: String(date.getFullYear()),
    location: translateContent('concerts', 'location', concert.location),
    name: translateContent('concerts', 'name', concert.name),
    fullLocation: translateContent('concerts', 'fullLocation', concert.fullLocation),
    collaborationInfo: translateContent('concerts', 'collaborationInfo', concert.collaborationInfo),
    googleMap: concert.googleMapUrl,
    expanded: expandedIds.value.has(concert.id)
  }
}).sort((a, b) => a.date.localeCompare(b.date)))
const upcomingEvents = computed(() => localizedConcerts.value.filter(concert => !concert.isPast))
const pastEvents = computed(() => localizedConcerts.value.filter(concert => concert.isPast))
function toggleEvent(id) {
  if (expandedIds.value.has(id)) expandedIds.value.delete(id)
  else expandedIds.value.add(id)
}
const togglePastEvent = toggleEvent
onMounted(async () => {
  try { concerts.value = await concertService.getAllConcerts() }
  catch (error) { logError(error, '공연 정보 로드') }
})
</script>

<style scoped>
/* 레이아웃 */
.concert {
  background: var(--color-background);
  color: var(--color-text);
  padding-top: 3rem;
  height: calc(100dvh - var(--navbar-height) - var(--website-footer-space));
  box-sizing: border-box;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 2fr;
  align-items: start;
  gap: 4rem;
  margin-left: auto;
  margin-right: auto;
  padding-left: 2rem;
  padding-right: 4rem;
}

/* 좌측 타이틀 */
.concert-title {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding-top: 2rem;
}

.concert-title h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: #CEC0A3; /* This seems specific (Gold/Beige). Keeping it unless I add --color-accent-gold */
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
}

/* 통합된 이벤트 컨테이너 */
.events-container {
  width: 100%;
  padding-top: 2rem;
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-gutter: stable;
  box-sizing: border-box;
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.events-container::-webkit-scrollbar {
  display: none;
}



/* 모바일 반응형 스타일 */
@media (max-width: 48rem) {
  .concert {
    display: block; /* 그리드 해제 */
    padding: 4rem 1.5rem var(--website-footer-space); /* 좌우 여백 축소 */
    height: auto; /* 전체 스크롤 허용 */
    overflow: visible;
    gap: 0;
  }

  .concert-title {
    padding-top: 0;
    margin-bottom: 2rem;
  }

  .concert-title h1 {
    font-size: 2.5rem; /* 모바일 타이틀 사이즈 조정 */
    text-align: left;
  }

  .events-container {
    padding-top: 0;
    overflow: visible;
    height: auto;
  }

}
</style>
