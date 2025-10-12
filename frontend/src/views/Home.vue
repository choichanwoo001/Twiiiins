<template>
  <div class="home fullpage-scroll">
    <!-- 히어로 섹션 (메인 이미지) -->
    <section class="hero-section">
      <div class="hero-image">
        <img src="../imgs/TwinsMusicApril2024-6 3.png" alt="TWIIIINS Performance">
      </div>
    </section>

    <!-- 이벤트 섹션 (붉은색 배경) -->
    <section class="events-section">
      <div class="events-container">
        <h2 class="events-title">UPCOMING EVENTS</h2>
        <div class="events-list">
          <div class="event-item" v-for="event in events" :key="event.id">
            <div class="event-date">{{ event.date }}</div>
            <div class="event-info">
              <div class="event-location">{{ event.location }}</div>
              <div class="event-name">{{ event.name }}</div>
            </div>
            <div class="event-arrow">→</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 하드코딩된 이벤트 데이터
const events = ref([
  {
    id: 1,
    date: 'July 30, 2026',
    location: 'Smaragd, Linz [AT]',
    name: 'TWIIIINS'
  },
  {
    id: 2,
    date: 'December 18, 2025',
    location: 'Korean Culture Center in Vienna',
    name: 'TWIIIINS'
  },
  {
    id: 3,
    date: 'September 18, 2025',
    location: 'Landestheater Salzburg',
    name: 'DER AUFHALTSAME AUFSTIEG DES'
  }
])
</script>

<style scoped>
/* 풀페이지 스크롤 컨테이너 */
.fullpage-scroll {
  scroll-snap-type: y mandatory;
  overflow-y: scroll;
  height: 100vh;
  position: relative;
  z-index: 1;
}

/* 히어로 섹션 */
.hero-section {
  height: 100vh;
  scroll-snap-align: start;
  position: relative;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

/* 이벤트 섹션 */
.events-section {
  height: 100vh;
  scroll-snap-align: start;
  background-color: #8B0000;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  z-index: 1;
}

.events-container {
  text-align: center;
  max-width: 800px;
  padding: 2rem;
}

.events-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 3rem;
  letter-spacing: 2px;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.event-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  transition: opacity 0.3s ease;
}

.event-item:hover {
  opacity: 0.8;
}

.event-date {
  font-size: 1.2rem;
  font-weight: 500;
  min-width: 180px;
  text-align: left;
}

.event-info {
  flex: 1;
  text-align: center;
}

.event-location {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
}

.event-name {
  font-size: 1.3rem;
  font-weight: bold;
}

.event-arrow {
  font-size: 1.5rem;
  font-weight: bold;
  min-width: 30px;
  text-align: right;
}
</style>

