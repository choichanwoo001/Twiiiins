<template>
  <div class="concert">
    <!-- 텍스트 섹션 -->
    <section class="concert-text">
      <h1>CONCERT</h1>
    </section>

    <!-- 이벤트 섹션 -->
    <section class="events-section">
      <!-- Upcoming Events -->
      <div class="events-category">
        <h2>Upcoming Events</h2>
        <div class="events-list">
          <div class="event-item" v-for="event in upcomingEvents" :key="event.id">
            <div class="event-date">{{ event.date }}</div>
            <div class="event-info">
              <div class="event-location">{{ event.location }}</div>
              <div class="event-name">{{ event.name }}</div>
            </div>
            <div class="event-expand">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- Past Events -->
      <div class="events-category">
        <h2>Past Events</h2>
        <div class="events-list">
          <div class="event-item" v-for="event in pastEvents" :key="event.id">
            <div class="event-date">{{ event.date }}</div>
            <div class="event-info">
              <div class="event-location">{{ event.location }}</div>
              <div class="event-name">{{ event.name }}</div>
            </div>
            <div class="event-expand">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 하드코딩된 이벤트 데이터
const upcomingEvents = ref([
  {
    id: 1,
    date: 'July 30, 2026',
    location: 'Smaragd, Linz [AT]',
    name: 'TWIIIINS Concert'
  },
  {
    id: 2,
    date: 'July 30, 2026',
    location: 'Smaragd, Linz [AT]',
    name: 'TWIIIINS Concert'
  },
  {
    id: 3,
    date: 'July 30, 2026',
    location: 'Smaragd, Linz [AT]',
    name: 'TWIIIINS Concert'
  }
])

const pastEvents = ref([
  {
    id: 1,
    date: 'July 30, 2026',
    location: 'Smaragd, Linz [AT]',
    name: 'TWIIIINS Concert'
  },
  {
    id: 2,
    date: 'July 30, 2026',
    location: 'Smaragd, Linz [AT]',
    name: 'TWIIIINS Concert'
  },
  {
    id: 3,
    date: 'July 30, 2026',
    location: 'Smaragd, Linz [AT]',
    name: 'TWIIIINS Concert'
  }
])
</script>

<style scoped>
.concert {
  background-color: #fafafa;
  color: #333;
  font-family: 'Arial', sans-serif;
  min-height: calc(100vh - 60px); /* 헤더 높이 제외 */
  margin-top: -60px; /* 헤더 패딩 상쇄 */
  padding-top: 60px; /* 헤더 높이만큼 상단 패딩 */
}

/* 텍스트 섹션 */
.concert-text {
  padding: 2rem 2rem 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  font-size: 4rem;
  font-weight: bold;
  color: #D4AF37;
  margin: 0;
  text-align: left;
  letter-spacing: 3px;
}

/* 이벤트 섹션 */
.events-section {
  padding: 0 2rem 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 4rem;
}

.events-category {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.events-category h2 {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin: 0;
  text-align: left;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.event-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 0;
  border-bottom: 1px solid #e0e0e0;
  transition: background-color 0.3s ease;
}

.event-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.event-item:last-child {
  border-bottom: none;
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
  cursor: pointer;
  transition: color 0.3s ease;
  min-width: 30px;
  text-align: right;
}

.event-expand:hover {
  color: #666;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .concert-text {
    padding: 2rem 1rem 3rem 1rem;
  }
  
  h1 {
    font-size: 2.5rem;
    text-align: center;
  }
  
  .events-section {
    padding: 0 1rem 3rem 1rem;
    gap: 3rem;
  }
  
  .event-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
    padding: 1rem 0;
  }
  
  .event-info {
    margin-left: 0;
  }
  
  .event-expand {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  h1 {
    font-size: 2rem;
  }
  
  .events-category h2 {
    font-size: 1.2rem;
  }
  
  .event-date {
    font-size: 0.9rem;
  }
  
  .event-location {
    font-size: 0.9rem;
  }
  
  .event-name {
    font-size: 0.8rem;
  }
}
</style>
