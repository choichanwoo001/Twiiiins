<template>
  <div class="media">
    <!-- 왼쪽 네비게이션 (모바일에서는 숨김) -->
    <div class="media-nav" v-if="!isMobile">
      <div class="nav-item main-title"
           :class="{ active: activeSection === 'music' || activeSection === '' }"
           @click="setActiveSection('music')">
        <h1>MUSIC</h1>
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'videos' }"
           @click="setActiveSection('videos')">
        <h2>VIDEOS</h2>
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'photos' }"
           @click="setActiveSection('photos')">
        <h2>PHOTOS</h2>
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'news' }"
           @click="setActiveSection('news')">
        <h2>NEWS</h2>
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'equipment' }"
           @click="setActiveSection('equipment')">
        <h2>EQUIPMENT</h2>
      </div>
    </div>

    <!-- 모바일 네비게이션 -->
    <div class="mobile-nav" v-if="isMobile">
      <div class="mobile-nav-item"
           :class="{ active: activeSection === 'music' || activeSection === '' }"
           @click="setActiveSection('music')">
        MUSIC
      </div>
      <div class="mobile-nav-item" 
           :class="{ active: activeSection === 'videos' }"
           @click="setActiveSection('videos')">
        VIDEOS
      </div>
      <div class="mobile-nav-item" 
           :class="{ active: activeSection === 'photos' }"
           @click="setActiveSection('photos')">
        PHOTOS
      </div>
      <div class="mobile-nav-item" 
           :class="{ active: activeSection === 'news' }"
           @click="setActiveSection('news')">
        NEWS
      </div>
      <div class="mobile-nav-item" 
           :class="{ active: activeSection === 'equipment' }"
           @click="setActiveSection('equipment')">
        EQUIPMENT
      </div>
    </div>

    <!-- 메인 콘텐츠 영역 -->
    <div class="media-content">
      <!-- MUSIC 섹션 (기본 표시) -->
      <div v-if="activeSection === 'music' || activeSection === ''" class="content-section">
        <div class="albums-grid">
          <div class="music-item" v-for="item in musicItems" :key="item.id">
            <div class="music-cover">
              <img :src="item.cover" :alt="item.title">
            </div>
            <div class="music-info">
              <div class="music-title">{{ item.title }}</div>
              <div class="music-artist">{{ item.artist }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- VIDEOS 섹션 -->
      <div v-if="activeSection === 'videos'" class="content-section">
        <div class="video-list" v-if="videos.length > 0">
          <div class="video-item" v-for="video in videos" :key="video.id">
            <div class="video-embed" v-if="isValidEmbedUrl(video.embedUrl)">
              <iframe
                :src="video.embedUrl"
                title="YouTube video player"
                style="border: none;"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                referrerpolicy="strict-origin-when-cross-origin"
                allowfullscreen
                loading="lazy"
              ></iframe>
            </div>
            <div v-else class="video-error">
              <p>Invalid video URL.</p>
            </div>
            <div class="video-info">
              <div class="video-title">{{ video.title }}</div>
              <div class="video-channel">TWIIIINS</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>No videos found.</p>
        </div>
      </div>

      <!-- PHOTOS 섹션 -->
      <div v-if="activeSection === 'photos'" class="content-section">
        <div class="photos-gallery">
          <div class="photo-group" v-for="group in photoGroups" :key="group.id">
            <div class="photo-group-title">{{ group.title }}</div>
            <div class="photos-grid">
              <div class="photo-item" 
                   v-for="photo in group.photos" 
                   :key="photo.src">
                <img :src="photo.src" :alt="photo.alt">
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- NEWS 섹션 -->
      <div v-if="activeSection === 'news'" class="content-section">
        <div class="news-list">
          <div class="news-item" v-for="news in newsList" :key="news.id">
            <div class="news-preview" @click="toggleNews(news.id)">
              <div class="news-date">{{ news.date }}</div>
              <div class="news-content">
                <div class="news-title">{{ news.title }}</div>
              </div>
              <div class="news-expand" :class="{ 'expanded': news.expanded }">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            
            <!-- 상세 정보 -->
            <div class="news-details" v-if="news.expanded">
              <div class="news-description">{{ news.description }}</div>
              
              <!-- 사진 섹션 -->
              <div class="news-images" v-if="news.imageUrls && news.imageUrls.length > 0">
                <ImageGrid 
                  :images="news.imageUrls" 
                  :alt-text-prefix="news.title" 
                  mobile-aspect-ratio="16 / 9"
                />
              </div>
            </div>
          </div>
        </div>
      </div>


      <!-- EQUIPMENT 섹션 -->
      <div v-if="activeSection === 'equipment'" class="content-section">
        <div class="equipment-grid">
          <div class="equipment-item" v-for="equipment in equipmentList" :key="equipment.id">
            <div class="equipment-image">
              <img :src="equipment.imageUrl || '../imgs/exphoto1.png'" :alt="equipment.name">
            </div>
            <div class="equipment-name">{{ equipment.name }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>

</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { 
  musicService, 
  videoService, 
  photoService, 
  newsService, 
  equipmentService 
} from '../services'
import { toAbsoluteUrl, formatDate } from '../utils/commonHelpers'
import { ImageGrid } from '../components/common'
import { logError } from '../utils/errorHandler'
import { useMobile } from '../composables/useMobile' 

import { useRoute } from 'vue-router'

const route = useRoute()

// 모바일 감지
// 모바일 감지
const { isMobile } = useMobile()

// 활성 섹션 상태
const activeSection = ref('')

// 섹션 변경 함수
const setActiveSection = (section) => {
  activeSection.value = section
}

// 쿼리 파라미터 변경 감지
watch(() => route.query.section, (newSection) => {
  if (newSection) {
    activeSection.value = newSection
  } 
})

// 음악 데이터
const musicItems = ref([])

// 비디오 데이터
const videos = ref([])

// 장비 데이터
const equipmentList = ref([])

// 음악 데이터 로드
const loadMusic = async () => {
  try {
    const musicData = await musicService.getAllMusic()
    
    if (musicData && musicData.length > 0) {
      musicItems.value = musicData.map(music => ({
        id: music.id,
        title: music.title,
        artist: music.artist,
        cover: music.coverUrl ? toAbsoluteUrl(music.coverUrl) : new URL('../imgs/music/time.png', import.meta.url).href
      }))
    } else {
      musicItems.value = []
    }
  } catch (error) {
    logError(error, '음악 데이터 로드')
    musicItems.value = []
  }
}

// embedUrl 유효성 검사
const isValidEmbedUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return false
  }
  
  const trimmedUrl = url.trim()
  if (trimmedUrl === '') {
    return false
  }
  
  // YouTube embed URL 패턴 확인
  // 올바른 형식: https://www.youtube.com/embed/VIDEO_ID
  // YouTube 비디오 ID는 11자리의 영숫자와 하이픈, 언더스코어만 포함 (실제로는 더 유연하게 처리)
  // 쿼리 파라미터나 해시가 있어도 허용 (split으로 처리)
  const cleanUrl = trimmedUrl.split('?')[0].split('#')[0]
  const youtubeEmbedPattern = /^https?:\/\/(www\.)?youtube\.com\/embed\/[\w-]+/
  const isValid = youtubeEmbedPattern.test(cleanUrl)
  
  // 디버깅용 (개발 환경에서만)

  
  return isValid
}

// 비디오 데이터 로드
const loadVideos = async () => {
  try {
    const videoData = await videoService.getAllVideos()
    
    if (Array.isArray(videoData)) {
      videos.value = videoData
        .filter(video => {
          if (!video || !video.id || !video.title) {
            if (import.meta.env.DEV) {
              console.warn('Missing required video fields:', video)
            }
            return false
          }
          
          if (!video.embedUrl) {
            if (import.meta.env.DEV) {
              console.warn('Missing video embedUrl:', video.id, video.title)
            }
            return false
          }
          
          const isValid = isValidEmbedUrl(video.embedUrl)
          if (!isValid && import.meta.env.DEV) {
            console.warn('Invalid embedUrl:', {
              id: video.id,
              title: video.title,
              embedUrl: video.embedUrl
            })
          }
          return isValid
        })
        .map(video => ({
          id: video.id,
          title: video.title,
          embedUrl: video.embedUrl.trim()
        }))
    } else {
      videos.value = []
    }
  } catch (error) {
    logError(error, '비디오 데이터 로드')
    videos.value = []
  }
}

// 페이지 로드 시 데이터 가져오기
// 페이지 로드 시 데이터 가져오기
onMounted(() => {
  // 초기 섹션 설정
  if (route.query.section) {
    activeSection.value = route.query.section
  } else {
    activeSection.value = 'music'
  }

  loadMusic()
  loadVideos()
  loadPhotoGroups()
  loadNews()
  loadEquipment()
})

// 사진 그룹 데이터
const photoGroups = ref([])

// 사진 그룹 데이터 로드
const loadPhotoGroups = async () => {
  try {
    const data = await photoService.getAllPhotoGroups()
    
    if (Array.isArray(data)) {
      photoGroups.value = data.map(group => ({
        id: group.id,
        title: group.title,
        photos: (group.photos && Array.isArray(group.photos)) ? group.photos.map(photo => ({
          src: toAbsoluteUrl(photo.imageUrl),
          alt: photo.altText || `Photo from ${group.title}`
        })) : []
      }))
    } else {
      photoGroups.value = []
    }
  } catch (error) {
    logError(error, '사진 그룹 데이터 로드')
    photoGroups.value = []
  }
}

// 장비 데이터 로드
const loadEquipment = async () => {
  try {
    const equipmentData = await equipmentService.getAllEquipment()
    equipmentList.value = equipmentData.map(equipment => ({
      id: equipment.id,
      name: equipment.name,
      imageUrl: toAbsoluteUrl(equipment.imageUrl)
    }))
  } catch (error) {
    logError(error, '장비 데이터 로드')
    equipmentList.value = []
  }
}

// 뉴스 데이터
const newsList = ref([])

// 뉴스 데이터 로드
const loadNews = async () => {
  try {
    const newsData = await newsService.getAllNews()
    newsList.value = newsData.map(news => ({
      id: news.id,
      date: formatDate(news.date, 'news'),
      title: news.title,
      description: news.description,
      imageUrls: news.imageUrls ? news.imageUrls.map(url => toAbsoluteUrl(url)) : [],
      expanded: false
    }))
  } catch (error) {
    logError(error, '뉴스 데이터 로드')
    newsList.value = []
  }
}

// 뉴스 토글 함수
const toggleNews = (newsId) => {
  const news = newsList.value.find(n => n.id === newsId)
  if (news) {
    news.expanded = !news.expanded
  }
}
</script>

<style scoped>
/* 데스크톱 스타일은 그대로 유지 */
.media {
  display: flex;
  background-color: var(--color-background);
  padding-top: 3rem;
  height: calc(100vh - var(--navbar-height));
  overflow: hidden;
}

/* 왼쪽 네비게이션 */
.media-nav {
  width: 34.375rem;
  padding: 2rem 2rem;
  background-color: var(--color-background);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 1.5rem;
  flex-shrink: 0;
  padding-top: 2rem;
}

.nav-item {
  cursor: pointer;
  transition: color 0.3s ease;
  text-align: left;
}

.nav-item h2 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: var(--color-text-lighter); /* #888 */
  margin: 0;
  line-height: 1;
  text-transform: uppercase;
  transition: all 0.5s ease;
  position: relative;
}

/* 활성 탭 스타일 */
.active-tab h2 {
  color: #154560; /* This seems specific (Navy). Keep? Or use primary? Keeping for now */
  font-weight: 400;
}
.nav-item.main-title {
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
}

.nav-item.main-title h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: rgba(251, 206, 123, 0.5); /* 비활성 상태: 더 연한 색상 */
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
  transition: color 0.3s ease;
}

.nav-item:hover h1,
.nav-item:hover h2 {
  color: rgba(251, 206, 123, 0.7); /* 호버 시 중간 색상 */
}

.nav-item.active h1,
.nav-item.active h2 {
  color: #FBCE7B; /* 활성 상태: 진한 색상 */
  font-weight: 400; /* 약간 더 굵게 -> Projects와 동일하게 400으로 변경 */
}

/* 메인 콘텐츠 영역 */
.media-content {
  flex: 1;
  padding: 2rem 2rem 2rem 1rem;
  background-color: white;
  min-height: 0; /* flex 컨테이너 내부 스크롤 허용 */
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-gutter: stable;
  box-sizing: border-box;
}

.content-section {
  width: 100%;
}

/* MUSIC 섹션 */
.albums-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2rem;
}

.music-item {
  text-align: left;
}

.music-cover {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  margin-bottom: 0.5rem;
  border-radius: 0;
  position: relative; /* relative positioning for absolute child if needed, though aspect-ratio usually handles it */
}

.music-cover img {
  width: 100%;
  height: 100%;
}
.music-info {
  margin-top: 1rem;
}

.album-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text); /* #1E1D1D */
  margin-bottom: 0.25rem;
}

.album-artist {
  font-size: 0.9rem;
  color: var(--color-text-tertiary); /* #666 */
  margin-bottom: 0.25rem;
}

.album-year {
  font-size: 0.8rem;
  color: var(--color-text-muted); /* #999 */
  margin-bottom: 1rem;
}

.album-links {
  display: flex;
  gap: 1rem;
}

.album-link {
  font-size: 0.8rem;
  color: var(--color-text-secondary); /* #555 */
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.album-link:hover {
  color: var(--color-text); /* #000 -> var(--color-text) */
  border-bottom-color: var(--color-text); /* #000 */
}
.video-embed {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #000;
  overflow: hidden;
  border-radius: 0.25rem; /* Optional: Slight rounding for better visuals */
}

.video-embed iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: none;
}

.video-error {
  padding: 2rem;
  text-align: center;
  color: #999;
  background: #f5f5f5;
  border-radius: 0.5rem;
}

/* 비디오 리스트 컨테이너 */
.video-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.video-info {
  flex: 1;
}

.video-title {
  font-size: 1.2rem;
  font-weight: bold;
}

.video-channel {
  font-size: 1rem;
  color: #666;
}

/* 비디오 없음 상태 */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 20rem;
  color: #999;
  font-size: 1.2rem;
}

/* PHOTOS 섹션 - 폴더별 그룹화 */
.photos-gallery {
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

.photo-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.photo-group-title {
  font-size: 0.9em;
  font-weight: 400;
  color: #FBCE7B;
  text-align: left;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12.5rem, 1fr));
  gap: 0.3rem;
}

@media (max-width: 48rem) {
  .photos-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.1rem;
  }

  .photo-item {
    aspect-ratio: 1 / 1;
    height: auto;
  }

  .photo-item img {
    height: 100%;
  }

  .photo-item:hover {
    transform: none;
  }
}

.photo-item {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease;
  border-radius: 0;
}

.photo-item:hover {
  transform: scale(1.02);
}

.photo-item img {
  width: 100%;
  height: 12.5rem;
  object-fit: cover;
  display: block;
}

/* NEWS 섹션 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.news-item {
  border-bottom: 0.0625rem solid #e0e0e0;
  transition: background-color 0.3s ease;
}

.news-item:last-child {
  border-bottom: none;
}

.news-preview {
  display: flex;
  align-items: center;
  padding: 1.5rem 0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.news-preview:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.news-date {
  font-family: 'Jost', sans-serif;
  font-weight: 500;
  color: var(--color-text); /* #1E1D1D */
  font-size: 1.1rem;
  min-width: 10rem;
}

.news-content {
  flex: 1;
}

.news-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.news-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: var(--color-text-secondary); /* #555 */
}

.news-toggle {
  color: var(--color-text-muted); /* #999 */
  transition: transform 0.3s ease;
}

.news-toggle.expanded {
  transform: rotate(180deg);
}

.news-description {
  font-size: 1rem;
  line-height: 1.6;
  color: var(--color-text-tertiary); /* #666 */
  margin-top: 1rem;
  white-space: pre-line;
}
.news-details {
  padding: 1.5rem 2rem 2rem 2rem; /* 상단, 오른쪽, 하단, 왼쪽 (좌우 여백 5rem -> 2rem 축소) */
  background-color: #f8f8f8;
  border-top: 0.0625rem solid #e0e0e0;
}

.news-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
  margin-bottom: 2rem;
}

/* 뉴스 사진 섹션 (프로젝트 디테일 스타일과 동일) */
.news-images {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #e0e0e0;
}

/* EQUIPMENT 섹션 */
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(18.75rem, 1fr));
  gap: 2rem;
}

.equipment-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.equipment-image {
  width: 100%;
  max-width: 37.5rem;
  overflow: hidden;
}

.equipment-image img {
  width: 100%;
  height: auto;
  object-fit: cover;
}

.equipment-name {
  margin-top: 1rem;
  font-size: 1rem;
  color: var(--color-text); /* #1E1D1D */
  text-align: center;
  font-weight: 500;
}
/* 모바일 전용 스타일 추가 */
@media (max-width: 48rem) {
  .media {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
    padding-top: 4rem;
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }
  
  /* 모바일에서 기존 nav는 숨김 (모바일 전용 메뉴 사용) */
  .media-nav {
    display: none;
  }
  
  /* 모바일 네비게이션 스타일 */
  .mobile-nav {
    display: flex;
    flex-direction: column;
    padding: 0;
    margin-bottom: 4rem;
    gap: 0.5rem;
  }

  .mobile-nav-item {
    font-size: 1.5rem;
    font-weight: 400;
    color: rgba(251, 206, 123, 0.6); /* 비활성 상태: 연한 주황색 + 투명도 */
    text-transform: uppercase;
    cursor: pointer;
    transition: all 0.3s ease;
    line-height: 1.2;
  }

  .mobile-nav-item.active {
    font-size: 2.5rem; /* Projects 모바일 타이틀과 동일한 크기 */
    letter-spacing: 0.12em;
    color: #FBCE7B;
    opacity: 1;
    word-wrap: break-word; /* 혹시라도 넘치면 줄바꿈, 하지만 폰트 조절로 한줄 유지 목표 */
  }

  .media-content {
    padding: 0;
    padding-bottom: 8rem;
    overflow: visible;
    padding-top: 0; /* 네비게이션이 위에 있으므로 상단 패딩 제거 */
  }
  
  /* 모바일 콘텐츠 그리드 조정 */
  .albums-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 1rem;
    row-gap: 2rem;
  }
  
  /* 모바일에서 이미지 크기 강제 */
  .music-cover {
    max-width: none;
    max-height: none;
    width: 100%;
    aspect-ratio: 1 / 1;
  }

  .music-title {
    font-size: 0.9rem;
    margin-bottom: 0.2rem;
    word-break: break-all;
    overflow-wrap: break-word;
  }

  .music-artist {
    font-size: 0.75rem;
    word-break: break-all;
    overflow-wrap: break-word;
  }

  /* News Section Mobile Optimization */
  .news-preview {
    flex-direction: column;
    align-items: flex-start;
    padding: 1.25rem 0; /* 20px */
    position: relative;
  }

  .news-date {
    width: 100%;
    margin-bottom: 0.5rem; /* 8px */
    font-size: 0.875rem; /* 14px */
    color: #999;
  }

  .news-content {
    margin-left: 0;
    width: 100%;
    padding-right: 2rem; /* Space for expand icon */
  }

  .news-title {
    font-size: 1.125rem; /* 18px */
    line-height: 1.4;
    word-break: keep-all;
  }

  .news-expand {
    position: absolute;
    right: 0;
    top: 1.25rem; /* Align with top padding */
    /* top: 50% transform: translateY(-50%) could work if date is small enough, 
       but fixing to top often looks cleaner with stacked layout */
  }

  /* News Details (Unfolded) Mobile Optimization */
  .news-details {
    padding: 1.5rem 1rem; /* Reduced side padding (16px) */
    background-color: #fafafa;
  }

  .news-description {
    font-size: 0.9375rem; /* 15px */
    line-height: 1.6;
    margin-bottom: 1.5rem;
    color: #444;
  }

  .news-images {
    margin-top: 1.5rem;
    padding-top: 1.5rem;
  }

  /* Equipment Section Mobile Optimization */
  .equipment-grid {
    display: flex;
    flex-direction: column;
    gap: 3rem; /* 48px */
  }

  .equipment-item {
    align-items: flex-end; /* Right align content */
    gap: 0.5rem;
  }

  .equipment-image {
    max-width: none;
    width: 100%; /* Full width */
  }

  .equipment-name {
    text-align: right;
    font-size: 0.875rem; /* 14px */
    width: 100%;
  }
  /* Mobile Video Section */
  .video-info {
    display: none;
  }
}
</style>
