<template>
  <div class="concert">
    <!-- 좌측 고정 타이틀 -->
    <aside class="concert-title">
      <h1>CONCERT</h1>
    </aside>

    <!-- 통합된 이벤트 섹션 -->
    <main class="events-container">
      <!-- Upcoming Events -->
      <ConcertSection 
        title="Upcoming Events" 
        :events="upcomingEvents" 
        @toggle="toggleEvent" 
      />

      <!-- Past Events -->
      <ConcertSection 
        title="Past Events" 
        :events="pastEvents" 
        :is-past="true"
        @toggle="togglePastEvent" 
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { concertService } from '../services'
import ConcertSection from '../components/ConcertSection.vue'
import { formatDate } from '../utils/commonHelpers'
import { logError } from '../utils/errorHandler'

// 이벤트 데이터
const upcomingEvents = ref([])
const pastEvents = ref([])

// 콘서트 데이터 로드
const loadConcerts = async () => {
  try {
    const allConcerts = await concertService.getAllConcerts()
    // concertService에서 이미 표준 응답의 data를 반환하므로 바로 사용 가능 (단, map을 돌려야 함)
    
    // 만약 service가 raw data를 반환한다면 그대로 map
    // concertService.getAllConcerts() -> returns response.data.data
    
    const formattedConcerts = allConcerts.map(concert => {
      const formattedDate = formatDate(concert.date, 'date', 'en-US')
      // 날짜 분리 (예: "July 30, 2026" -> ["July 30,", "2026"])
      const [dateMain, dateYear] = formattedDate.split(/(?=\s\d{4})/).map(s => s.trim())
      
      return {
        id: concert.id,
        date: formattedDate,
        dateMain: dateMain || formattedDate,
        dateYear: dateYear || '',
        location: concert.location,
        name: concert.name,
        expanded: false,
        startTime: concert.startTime || '',
        ticketInfo: concert.ticketInfo || '',
        fullLocation: concert.fullLocation || '',
        googleMap: concert.googleMapUrl || '',
        collaborationInfo: concert.collaborationInfo || '',
        isPast: concert.isPast
      }
    })
    
    // 다가오는 이벤트와 과거 이벤트로 분리하고 날짜순으로 정렬
    upcomingEvents.value = formattedConcerts
      .filter(concert => !concert.isPast)
      .sort((a, b) => new Date(a.date) - new Date(b.date)) // 오름차순 정렬 (가장 이른 날짜가 위로)
    
    pastEvents.value = formattedConcerts
      .filter(concert => concert.isPast)
      .sort((a, b) => new Date(a.date) - new Date(b.date)) // 오름차순 정렬 (가장 이른 날짜가 위로)
  } catch (error) {
    logError(error, '콘서트 데이터 로드')
    // 에러 시 빈 배열로 초기화
    upcomingEvents.value = []
    pastEvents.value = []
  }
}

// 토글 함수
const toggleEvent = (eventId) => {
  const event = upcomingEvents.value.find(e => e.id === eventId)
  if (event) {
    event.expanded = !event.expanded
  }
}

// 과거 이벤트 토글 함수
const togglePastEvent = (eventId) => {
  const event = pastEvents.value.find(e => e.id === eventId)
  if (event) {
    event.expanded = !event.expanded
  }
}

// 페이지 로드 시 데이터 가져오기
onMounted(() => {
  loadConcerts()
})
</script>

<style scoped>
/* 레이아웃 */
.concert {
  background: var(--color-background);
  color: var(--color-text);
  padding-top: 6rem;
  height: calc(100vh - 3.75rem);
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
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-gutter: stable;
  box-sizing: border-box;
}



/* 모바일 반응형 스타일 */
@media (max-width: 48rem) {
  .concert {
    display: block; /* 그리드 해제 */
    padding: 6rem 1.5rem 2rem 1.5rem; /* 좌우 여백 축소 */
    height: auto; /* 전체 스크롤 허용 */
    overflow: visible;
    gap: 0;
  }

  .concert-title {
    padding-top: 1rem;
    margin-bottom: 3rem;
  }

  .concert-title h1 {
    font-size: 2.22rem; /* 모바일 타이틀 사이즈 조정 */
    text-align: left;
  }

  .events-container {
    padding-top: 0;
    overflow: visible;
    height: auto;
  }

}
</style>
