<template>
  <div class="concert">
    <!-- 좌측 고정 타이틀 -->
    <aside class="concert-title">
      <h1>CONCERT</h1>
    </aside>

    <!-- 중간 섹션 제목 -->
    <aside class="section-title">
      <h2>Upcoming Events</h2>
    </aside>

    <!-- 우측 이벤트 목록 -->
    <main class="events-content">
      <div class="events-list">
        <div class="event-item" v-for="event in upcomingEvents" :key="event.id">
          <div class="event-preview" @click="toggleEvent(event.id)">
            <div class="event-date">{{ event.date }}</div>
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
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 토글 함수
const toggleEvent = (eventId, category = 'upcoming') => {
  const events = category === 'past' ? pastEvents.value : upcomingEvents.value
  const event = events.find(e => e.id === eventId)
  if (event) {
    event.expanded = !event.expanded
  }
}

// 하드코딩된 이벤트 데이터 (상세 정보 포함)
const upcomingEvents = ref([
  {
    id: 1,
    date: 'October 25, 2025',
    location: 'Schloss Salon, Linz [AT]',
    name: 'TWIIIINS Concert',
    expanded: false,
    startTime: '20:30',
    ticketInfo: 'https://aeskil.at/',
    fullLocation: 'Schloss Eschelberg, Eschelberg 1, 4112 St. Gotthard im Mühlkreis',
    googleMap: 'https://maps.app.goo.gl/qSDyev6zUo7BP1YEA',
    collaborationInfo: ''
  },
  {
    id: 2,
    date: 'November 26, 2025',
    location: 'Märchenbazar, Munich [DE]',
    name: 'TWIIIINS Concert',
    expanded: false,
    startTime: '',
    ticketInfo: 'https://maerchenbazar.de/',
    fullLocation: 'Olympiapark München, Spiridon-Louis-Ring 21, 80809 München, Germany',
    googleMap: 'https://maps.app.goo.gl/1Qk3S7JeBzhiDtNH7',
    collaborationInfo: ''
  },
  {
    id: 3,
    date: 'December 6, 2025',
    location: 'Kopfbau, Munich [DE]',
    name: 'TWIIIINS Concert',
    expanded: false,
    startTime: '',
    ticketInfo: '',
    fullLocation: '',
    googleMap: '',
    collaborationInfo: ''
  },
  {
    id: 4,
    date: 'December 18, 2025',
    location: 'Korea Kulturzentrum, Vienna [AT]',
    name: 'TWIIIINS Concert',
    expanded: false,
    startTime: '18:00',
    ticketInfo: 'https://vienna.korean-culture.org/vienna.korean-culture.html',
    fullLocation: 'Kärntner Straße 43, 1010 Wien, Österreich',
    googleMap: 'https://maps.app.goo.gl/oupu1vYxPmjBmSU78',
    collaborationInfo: ''
  },
  {
    id: 5,
    date: 'August 1, 2026',
    location: 'Herzberg Festival [DE]',
    name: 'TWIIIINS Concert',
    expanded: false,
    startTime: '',
    ticketInfo: 'https://herzberg-festival.com/',
    fullLocation: 'Hofhuhnstadt, 36287 Breitenbach am Herzberg, Germany',
    googleMap: 'https://maps.app.goo.gl/hW5iV3QHHnXxhU2d6',
    collaborationInfo: ''
  }
])

const pastEvents = ref([
  {
    id: 1,
    date: 'February 22, 2025',
    location: 'Salzburg State Theatre, Salzburg [AT]',
    name: 'Arturo Ui Performance',
    expanded: false,
    startTime: '19:30',
    ticketInfo: 'https://salzburger-landestheater.at/',
    fullLocation: 'Salzburg State Theatre, Schwarzstraße 22, 5020 Salzburg',
    googleMap: 'https://maps.app.goo.gl/salzburg-example',
    collaborationInfo: 'Collaboration with Salzburger Landestheater'
  },
  {
    id: 2,
    date: 'June 10, 2024',
    location: 'Göttingen Concert Hall, Göttingen [DE]',
    name: 'TWIIIINS Concert',
    expanded: false,
    startTime: '20:00',
    ticketInfo: 'https://goettingen-music.de/',
    fullLocation: 'Göttingen Concert Hall, Theaterplatz 1, 37073 Göttingen',
    googleMap: 'https://maps.app.goo.gl/goettingen-example',
    collaborationInfo: ''
  }
])
</script>

<style scoped>
/* 레이아웃 */
.concert {
  background: #fafafa;
  color: #222;
  padding-top: 6rem;
  height: calc(100vh - 60px);
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 1fr 2fr;
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
  font-weight: 500;
  letter-spacing: 0.12em;
  color: #D4AF37;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
}

/* 중간 섹션 제목 */
.section-title {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding-top: 2rem;
}

.section-title h2 {
  font-size: 1.5rem;
  color: #333;
  padding-top: 1rem;
  padding-left: 8rem;
}

/* 우측 이벤트 콘텐츠 */
.events-content {
  width: 100%;
  padding-top: 2rem;
  min-height: 0; /* 그리드 아이템 내부 스크롤 허용 */
  height: 100%;
  overflow-y: auto;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.event-item {
  border-bottom: 1px solid #e0e0e0;
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
  min-width: 150px;
  text-align: left;
}

.event-info {
  flex: 1;
  text-align: left;
  margin-left: 2rem;
}

.event-location {
  font-size: 1rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.25rem;
}

.event-name {
  font-size: 0.9rem;
  color: #666;
}

.event-expand {
  color: #999;
  transition: all 0.3s ease;
  min-width: 30px;
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
  border-top: 1px solid #e0e0e0;
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
  min-width: 200px;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  font-size: 0.9rem;
  color: #333;
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

</style>
