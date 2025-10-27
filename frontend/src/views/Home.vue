<template>
  <div class="home fullpage-scroll" ref="scrollContainer">
    <!-- 히어로 섹션 (메인 이미지) -->
    <section class="hero-section">
      <div class="hero-image">
        <img src="../imgs/home.png" alt="TWIIIINS Performance">
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

    <!-- 페이지 인디케이터 -->
    <div class="page-indicators">
      <div 
        v-for="(section, index) in sections" 
        :key="index"
        class="indicator"
        :class="{ active: currentSection === index }"
        @click="scrollToSection(index)"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from '../api/axios'

// 이벤트 데이터
const events = ref([])

// 콘서트 데이터 로드
const loadEvents = async () => {
  try {
    const response = await axios.get('/api/concerts')
    // 다가오는 이벤트만 필터링 (isPast: false)
    events.value = response.data
      .filter(concert => !concert.isPast)
      .map(concert => ({
        id: concert.id,
        date: new Date(concert.date).toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'long',
          day: 'numeric'
        }),
        location: concert.location,
        name: concert.name
      }))
  } catch (error) {
    // 에러 시 빈 배열로 설정
    events.value = []
  }
}

// 페이지 인디케이터 관련
const scrollContainer = ref(null)
const currentSection = ref(0)
const sections = ref(['hero', 'events'])

// 섹션으로 스크롤하는 함수
const scrollToSection = (index) => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollTo({
      top: index * window.innerHeight,
      behavior: 'smooth'
    })
  }
}

// 스크롤 이벤트 리스너
const handleScroll = () => {
  if (scrollContainer.value) {
    const scrollTop = scrollContainer.value.scrollTop
    const sectionHeight = window.innerHeight
    const newSection = Math.round(scrollTop / sectionHeight)
    currentSection.value = Math.min(newSection, sections.value.length - 1)
    
    // 현재 섹션에 따라 body에 클래스 추가/제거
    if (currentSection.value === 1) {
      document.body.classList.add('events-section-active')
    } else {
      document.body.classList.remove('events-section-active')
    }
  }
}

// 페이지 진입 시 body에 클래스 추가, 페이지 나갈 때 제거
onMounted(() => {
  document.body.classList.add('has-fullpage-scroll')
  if (scrollContainer.value) {
    scrollContainer.value.addEventListener('scroll', handleScroll)
  }
  loadEvents() // 이벤트 데이터 로드
})

onUnmounted(() => {
  document.body.classList.remove('has-fullpage-scroll')
  document.body.classList.remove('events-section-active')
  if (scrollContainer.value) {
    scrollContainer.value.removeEventListener('scroll', handleScroll)
  }
})
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

/* 스크롤바 숨기기 */
.fullpage-scroll::-webkit-scrollbar {
  display: none;
}

.fullpage-scroll {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 히어로 섹션 */
.hero-section {
  height: 100vh;
  scroll-snap-align: start;
  position: relative;
  overflow: hidden;
  margin-top: 0;
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
  max-width: 50rem;
  padding: 1.5rem;
  width: 90%;
}

.events-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 2rem;
  letter-spacing: 0.0625rem;
  text-align: center;
  margin-left: 8.75rem;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 경계선은 삭제 */
.event-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 1rem 0;
  transition: opacity 0.3s ease;
  gap: 1rem;
}

.event-item:hover {
  opacity: 0.8;
}

.event-date {
  font-size: 0.9rem;
  font-weight: 500;
  width: 7.5rem;
  text-align: left;
  flex-shrink: 0;
  margin-right: 1.25rem;
}

.event-info {
  flex: 1;
  text-align: left;
  min-width: 0;
  padding-left: 0;
}

.event-location {
  font-size: 0.85rem;
  margin-bottom: 0.3rem;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.event-name {
  font-size: 1rem;
  font-weight: bold;
  word-wrap: break-word;
  overflow-wrap: break-word;
  line-height: 1.2;
}

.event-arrow {
  font-size: 1.2rem;
  font-weight: bold;
  min-width: 1.25rem;
  text-align: right;
  flex-shrink: 0;
}

/* 페이지 인디케이터 */
.page-indicators {
  position: fixed;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.indicator {
  width: 0.75rem;
  height: 0.75rem;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 0.125rem solid rgba(255, 255, 255, 0.8);
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: scale(1.2);
}

.indicator.active {
  background: rgba(255, 255, 255, 1);
  transform: scale(1.3);
  box-shadow: 0 0 0.625rem rgba(255, 255, 255, 0.5);
}
</style>

