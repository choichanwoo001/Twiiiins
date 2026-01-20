<template>
  <div class="home fullpage-scroll" ref="scrollContainer">
    <!-- 히어로 섹션 (메인 이미지) -->
    <section class="hero-section">
      <div class="hero-image">
        <picture>
          <source media="(max-width: 48rem)" :srcset="mobileHomeImg">
          <img :src="homeImg" alt="TWIIIINS Performance">
        </picture>
      </div>
    </section>

    <!-- 이벤트 섹션 (붉은색 배경) -->
    <section class="events-section">
      <div class="events-container">
        <h2 class="events-title">UPCOMING EVENTS</h2>
        <div class="events-list" v-if="events && events.length > 0">
          <div class="event-item" v-for="event in events" :key="event.id">
            <div class="event-date">
              <span>{{ event.dateMonthDay }}</span>
              <span>{{ event.dateYear }}</span>
            </div>
            <div class="event-info">
              <div class="event-location">{{ event.location }}</div>
              <div class="event-name">{{ event.name }}</div>
            </div>
            <div class="event-arrow" @click="goToConcert">↗</div>
          </div>
        </div>
        <div v-else class="no-events">
          <p>예정된 공연이 없습니다.</p>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useConcertStore } from '../stores'
import { useRouter } from 'vue-router'
import { formatDate } from '../utils/commonHelpers'
import { logError } from '../utils/errorHandler'

// 이미지 리소스 임포트
import homeImg from '../imgs/home.png'
import mobileHomeImg from '../imgs/mobile_home.png'

// 스토어 사용
const concertStore = useConcertStore()
const router = useRouter()

// 이벤트 데이터 (스토어에서 가져온 데이터를 변환)
const events = computed(() => {
  return concertStore.upcomingConcerts
    .map(concert => {
      // 날짜 포맷팅: "July 30, 2026"
      const dateObj = new Date(concert.date);
      // 날짜 부분: "July 30,"
      const monthDay = new Intl.DateTimeFormat('en-US', { month: 'long', day: 'numeric' }).format(dateObj) + ',';
      // 연도 부분: "2026"
      const year = dateObj.getFullYear();
      
      return {
        id: concert.id,
        date: formatDate(concert.date, 'date', 'en-US'),
        dateMonthDay: monthDay,
        dateYear: year,
        dateValue: dateObj, 
        location: concert.location,
        name: concert.name
      }
    })
    .sort((a, b) => a.dateValue - b.dateValue) // 오름차순 정렬 (가장 이른 날짜가 위로)
})

// 콘서트 데이터 로드
const loadEvents = async () => {
  try {
    await concertStore.loadConcerts()
  } catch (error) {
    // API 호출 실패해도 화면은 표시되도록 함
    logError(error, '공연 정보 로드')
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

// Concert 페이지로 이동하는 함수
const goToConcert = () => {
  router.push('/concerts')
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
  height: 100dvh; /* 모바일 주소창 대응 */
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
  height: 100dvh; /* 모바일 주소창 대응 */
  scroll-snap-align: start;
  position: relative;
  overflow: hidden;
  margin-top: 0;
}

.hero-image {
  width: 100%;
  height: 100%;
}



.hero-image picture {
  display: block;
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
  height: 100dvh; /* 모바일 주소창 대응 */
  scroll-snap-align: start;
  background-color: #943C31;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  z-index: 2; /* Hero 이미지 위에 확실히 올라오도록 설정 */
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
  text-align: left;
  margin-left: 14.25rem;
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
  width: 12rem;
  min-width: 12rem;
  text-align: left;
  flex-shrink: 0;
  margin-right: 1.25rem;
  white-space: nowrap;
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
  font-size: 1.5rem; /* 화살표 크기 키움 */
  font-weight: 300; /* 얇게 */
  min-width: 1.5rem;
  text-align: right;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.3s ease;
  transform: rotate(0deg); /* 초기 상태 */
}

.event-arrow:hover {
  transform: scale(1.2) translate(2px, -2px); /* 호버 시 우상단으로 살짝 이동 */
  color: #ffd700;
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
  border-radius: 50%;
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

.no-events {
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  padding: 2rem;
}

.no-events p {
  font-size: 1rem;
}

/* 모바일 반응형 스타일 */
/* 모바일 반응형 스타일 */
@media (max-width: 48rem) {
  .events-title {
    margin-left: 0;
    text-align: center;
    font-size: 1.2rem;
    margin-bottom: 2rem; /* 간격 조정 */
    font-weight: 350;
    letter-spacing: 0.05rem;
    width: 100%;
    flex-shrink: 0; /* 크기 줄어들지 않도록 설정 */
  }

  .events-container {
    width: 85%;
    max-width: 100%;
    padding: 1rem;
    display: flex;
    flex-direction: column;
    height: 100%;
    justify-content: flex-start; /* 상단부터 채움 */
    padding-top: 6rem; /* 상단 여백 확보 (헤더 등을 위해) */
    padding-bottom: 5rem; /* SNS 링크 공간 */
    margin: 0 auto;
    overflow: hidden; /* 컨테이너 자체는 스크롤 없음 */
  }

  .events-list {
    width: 100%;
    gap: 2rem;
    overflow-y: auto; /* 내부 스크롤 허용 */
    flex: 1; /* 남은 공간 차지 */
    padding-right: 0.5rem; /* 스크롤 공간 */
    
    /* 스크롤바 숨기기 */
    -ms-overflow-style: none; /* IE and Edge */
    scrollbar-width: none; /* Firefox */
  }
  
  .events-list::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera */
  }

  .event-item {
    align-items: flex-start;
    padding: 0;
    justify-content: flex-start;
    width: 100%;
    position: relative;
    padding-bottom: 1rem; /* 아이템 하단 여백 추가 */
  }

  .event-date {
    min-width: 4.5rem;
    width: 4.5rem;
    font-size: 0.75rem;
    margin-right: 1.2rem;
    line-height: 1.6;
    white-space: normal;
    display: flex;
    flex-direction: column;
    text-align: left;
    color: rgba(255, 255, 255, 0.9);
    font-weight: 300;
  }

  .event-info {
    font-size: 0.8rem;
    line-height: 1.6;
    padding-top: 0;
    flex: 1;
    min-width: 0;
    padding-right: 2rem;
  }

  .event-location {
    font-size: 0.8rem;
    margin-bottom: 0.2rem;
    font-weight: 300;
    word-break: keep-all;
    word-wrap: break-word;
  }
  
  .event-name {
    font-size: 0.8rem;
    font-weight: 300;
    word-break: keep-all;
    word-wrap: break-word;
  }
  
  .event-arrow {
    position: absolute;
    right: 0;
    top: 0;
    margin-left: 0;
    font-size: 1.2rem;
    font-weight: 100;
  }
  
  /* 모바일에서 인디케이터 숨김 */
  .page-indicators {
    display: none;
  }
  
  /* Hero 이미지 모바일 최적화 */
  .hero-section {
    background-color: #fff;
  }

  .hero-image {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .hero-image img {
    object-position: center top; /* 상단(얼굴) 중심으로 배치 */
    object-fit: cover; /* 화면을 꽉 채움 (잘림 발생 가능하지만 user requested full screen) */
    width: 100%;
    height: 100%;
    transform: none;
    margin: 0;
  }
}
</style>

