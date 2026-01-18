<template>
  <div class="concert">
    <!-- 좌측 고정 타이틀 -->
    <aside class="concert-title">
      <h1>CONCERT</h1>
    </aside>

    <!-- 통합된 이벤트 섹션 -->
    <main class="events-container">
      <!-- Upcoming Events -->
      <div class="events-section">
        <div class="section-layout">
          <h2 class="section-title">Upcoming Events</h2>
          <div class="events-list">
          <div class="event-item" v-for="event in upcomingEvents" :key="event.id">
            <div class="event-preview" @click="toggleEvent(event.id)">
              <div class="event-date">
                <span class="date-main">{{ event.dateMain }}</span>
                <span class="date-year">{{ event.dateYear }}</span>
              </div>
              <div class="event-info">
                <div class="event-location">{{ event.location }}</div>
                <div class="event-name">{{ event.name }}</div>
              </div>
              <div class="event-expand" :class="{ 'expanded': event.expanded }">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            
            <!-- 상세 정보 -->
            <div class="event-details" v-if="event.expanded">
              <div class="detail-row">
                <span class="detail-label">Concert starts at:</span>
                <span class="detail-value">{{ event.startTime }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Ticket Info:</span>
                <a :href="event.ticketInfo" target="_blank" class="detail-link">{{ event.ticketInfo }}</a>
              </div>
              <div class="detail-row">
                <span class="detail-label">Location:</span>
                <span class="detail-value">{{ event.fullLocation }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Google Map:</span>
                <a :href="event.googleMap" target="_blank" class="detail-link">View on Google Maps</a>
              </div>
              <div class="detail-row" v-if="event.collaborationInfo">
                <span class="detail-label">Concert information (only for collaboration):</span>
                <span class="detail-value">{{ event.collaborationInfo }}</span>
              </div>
            </div>
          </div>
          </div>
        </div>
      </div>

      <!-- Past Events -->
      <div class="events-section">
        <div class="section-layout">
          <h2 class="section-title">Past Events</h2>
          <div class="events-list">
          <div class="event-item" v-for="event in pastEvents" :key="event.id">
            <div class="event-preview" @click="togglePastEvent(event.id)">
              <div class="event-date">
                <span class="date-main">{{ event.dateMain }}</span>
                <span class="date-year">{{ event.dateYear }}</span>
              </div>
              <div class="event-info">
                <div class="event-location">{{ event.location }}</div>
                <div class="event-name">{{ event.name }}</div>
              </div>
              <div class="event-expand" :class="{ 'expanded': event.expanded }">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            
            <!-- 상세 정보 -->
            <div class="event-details" v-if="event.expanded">
              <div class="detail-row">
                <span class="detail-label">Concert started at:</span>
                <span class="detail-value">{{ event.startTime }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Location:</span>
                <span class="detail-value">{{ event.fullLocation }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Google Map:</span>
                <a :href="event.googleMap" target="_blank" class="detail-link">View on Google Maps</a>
              </div>
              <div class="detail-row" v-if="event.collaborationInfo">
                <span class="detail-label">Concert information (only for collaboration):</span>
                <span class="detail-value">{{ event.collaborationInfo }}</span>
              </div>
            </div>
          </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../api/axios'
import { formatDate } from '../utils/commonHelpers'
import { logError } from '../utils/errorHandler'

// 이벤트 데이터
const upcomingEvents = ref([])
const pastEvents = ref([])

// 콘서트 데이터 로드
const loadConcerts = async () => {
  try {
    const response = await axios.get('/concerts')
    const allConcerts = response.data.data.map(concert => {
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
    upcomingEvents.value = allConcerts
      .filter(concert => !concert.isPast)
      .sort((a, b) => new Date(a.date) - new Date(b.date)) // 오름차순 정렬 (가장 이른 날짜가 위로)
    
    pastEvents.value = allConcerts
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
  background: #fff;
  color: #222;
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
  color: #CEC0A3;
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

/* 이벤트 섹션 */
.events-section {
  margin-bottom: 3rem;
}

/* 섹션 레이아웃 (좌우 배치) */
.section-layout {
  display: grid;
  grid-template-columns: 12.5rem 1fr;
  gap: 2rem;
  align-items: start;
}

/* 섹션 제목 */
.section-title {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
  font-weight: 500;
  padding-top: 1.5rem;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.event-item {
  border-bottom: 0.0625rem solid #e0e0e0;
  transition: background-color 0.3s ease;
}

.event-item:last-child {
  border-bottom: none;
}

.event-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.event-preview:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.event-date {
  font-size: 1rem;
  color: #555;
  min-width: 9.375rem;
  text-align: left;
  display: flex;
  flex-direction: column;
}

.date-main {
  display: inline;
}

.date-year {
  display: inline;
  margin-left: 0.25rem;
}

.event-info {
  flex: 1;
  text-align: left;
  margin-left: 2rem;
}

.event-location {
  font-size: 1rem;
  font-weight: bold;
  color: #1E1D1D;
  margin-bottom: 0.25rem;
}

.event-name {
  font-size: 0.9rem;
  color: #666;
}

.event-expand {
  color: #999;
  transition: all 0.3s ease;
  min-width: 1.875rem;
  text-align: right;
}

.event-expand.expanded {
  transform: rotate(180deg);
}

.event-expand:hover {
  color: #666;
}

/* 상세 정보 스타일 */
.event-details {
  padding: 1.5rem 0 2rem 0;
  background-color: #f8f8f8;
  border-top: 0.0625rem solid #e0e0e0;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 1rem;
  gap: 1rem;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 0.9rem;
  color: #666;
  min-width: 12.5rem;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  font-size: 0.9rem;
  color: #1E1D1D;
  flex: 1;
}

.detail-link {
  font-size: 0.9rem;
  color: #8B0000;
  text-decoration: none;
  transition: color 0.2s ease;
}

.detail-link:hover {
  color: #A00000;
  text-decoration: underline;
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

  .section-layout {
    display: flex;
    flex-direction: column; /* 세로 배치 */
    gap: 1rem;
  }

  .section-title {
    padding-top: 0;
    margin-bottom: 0.5rem;
    font-size: 1.2rem;
    font-weight: 300;
  }

  .event-preview {
    align-items: flex-start; /* 상단 정렬 */
    padding: 1.25rem 0;
  }

  .event-date {
    min-width: auto; /* 너비 자동 */
    margin-right: 0.75rem;
    display: block; /* 블록 요소로 변경하여 한 줄 처리 용이하게 */
    white-space: nowrap; /* 줄바꿈 방지 */
  }

  .date-main {
    font-weight: 500;
    color: #1E1D1D;
    font-size: 0.8rem; /* 폰트 축소 */
  }

  .date-year {
    display: inline; /* 한 줄 표시 */
    margin-left: 0.2rem;
    color: #888;
    font-size: 0.8rem; /* 폰트 축소 */
    margin-top: 0;
  }

  .event-info {
    margin-left: 0; /* 좌측 여백 제거 */
    margin-right: 0.5rem; /* 우측 여백 조정 */
    min-width: 0; /* flex 자식 요소 말줄임 처리를 위해 */
  }
  
  .event-location {
    font-size: 0.95rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .event-name {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* 상세 정보 레이아웃 가로 배치에서 세로 배치로 변경 */
  .detail-row {
    flex-direction: column;
    gap: 0.25rem;
    margin-bottom: 0.75rem;
  }

  .detail-label {
    min-width: auto;
    width: 100%;
    font-size: 0.8rem;
    color: #888;
  }

  /* 모바일에서 상세 정보 텍스트 크기 조정 */
  .detail-value, .detail-link {
    font-size: 0.9rem;
    word-break: break-all; /* 긴 URL 등 줄바꿈 */
  }
}
</style>
