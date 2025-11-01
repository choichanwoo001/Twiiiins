<template>
  <div class="media">
    <!-- 왼쪽 네비게이션 -->
    <div class="media-nav">
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

    <!-- 메인 콘텐츠 영역 -->
    <div class="media-content">
      <!-- MUSIC 섹션 (기본 표시) -->
      <div v-if="activeSection === 'music' || activeSection === ''" class="content-section">
        <div class="music-content-wrapper">
          <!-- Artist Profile (왼쪽) -->
          <div class="artist-profile-item">
            <div class="music-cover">
              <img :src="artistProfileImage" alt="Artist Profile">
            </div>
            <div class="music-info">
              <div class="music-title">Artist Profile</div>
              <div class="music-artist">TWIIIINS</div>
            </div>
          </div>

          <!-- Music Grid (오른쪽) -->
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
              <p>비디오 URL이 유효하지 않습니다.</p>
            </div>
            <div class="video-info">
              <div class="video-title">{{ video.title }}</div>
              <div class="video-channel">TWIIIINS</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>등록된 비디오가 없습니다.</p>
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
import { ref, computed, onMounted } from 'vue'
import axios from '../api/axios'

// 백엔드 절대 URL 생성 유틸
const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
const toAbsoluteUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
}

// 활성 섹션 상태
const activeSection = ref('')

// 섹션 변경 함수
const setActiveSection = (section) => {
  activeSection.value = section
}

// Artist Profile 이미지 경로
const artistProfileImage = computed(() => {
  return new URL('../imgs/home.png', import.meta.url).href
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
    const response = await axios.get('/api/media/music')
    
    if (response.data.data && response.data.data.length > 0) {
      musicItems.value = response.data.data.map(music => ({
        id: music.id,
        title: music.title,
        artist: music.artist,
        cover: music.coverUrl ? toAbsoluteUrl(music.coverUrl) : new URL('../imgs/music/time.png', import.meta.url).href
      }))
    } else {
      musicItems.value = []
    }
  } catch (error) {
    console.error('음악 데이터 로드 실패:', error)
    musicItems.value = []
  }
}

// embedUrl 유효성 검사
const isValidEmbedUrl = (url) => {
  if (!url || typeof url !== 'string') return false
  const trimmedUrl = url.trim()
  if (trimmedUrl === '') return false
  
  // YouTube embed URL 패턴 확인
  // 올바른 형식: https://www.youtube.com/embed/VIDEO_ID
  const youtubeEmbedPattern = /^https?:\/\/(www\.)?youtube\.com\/embed\/[a-zA-Z0-9_-]+/
  return youtubeEmbedPattern.test(trimmedUrl)
}

// 비디오 데이터 로드
const loadVideos = async () => {
  try {
    const response = await axios.get('/api/media/videos')
    const videoData = response.data?.data || []
    
    if (Array.isArray(videoData)) {
      videos.value = videoData
        .filter(video => {
          if (!video || !video.id || !video.title) return false
          // embedUrl이 있어도 유효한지 확인
          if (!video.embedUrl || !isValidEmbedUrl(video.embedUrl)) {
            console.warn('유효하지 않은 embedUrl:', video.embedUrl, 'video id:', video.id)
            return false
          }
          return true
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
    console.error('비디오 데이터 로드 실패:', error)
    videos.value = []
  }
}

// 페이지 로드 시 데이터 가져오기
onMounted(() => {
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
    const response = await axios.get('/api/media/photo-groups')
    photoGroups.value = response.data.data.map(group => ({
      id: group.id,
      title: group.title,
      photos: group.photos ? group.photos.map(photo => ({
        src: toAbsoluteUrl(photo.imageUrl),
        alt: photo.title || `Photo from ${group.title}`
      })) : []
    }))
  } catch (error) {
    console.error('사진 그룹 데이터 로드 실패:', error)
    photoGroups.value = []
  }
}

// 장비 데이터 로드
const loadEquipment = async () => {
  try {
    const response = await axios.get('/api/media/equipment')
    equipmentList.value = response.data.data.map(equipment => ({
      id: equipment.id,
      name: equipment.name,
      imageUrl: toAbsoluteUrl(equipment.imageUrl)
    }))
  } catch (error) {
    console.error('장비 데이터 로드 실패:', error)
    equipmentList.value = []
  }
}

// 뉴스 데이터
const newsList = ref([])

// 뉴스 데이터 로드
const loadNews = async () => {
  try {
    const response = await axios.get('/api/media/news')
    newsList.value = response.data.data.map(news => ({
      id: news.id,
      date: formatNewsDate(news.date),
      title: news.title,
      description: news.description,
      expanded: false
    }))
  } catch (error) {
    console.error('뉴스 데이터 로드 실패:', error)
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

// 뉴스 날짜 포맷팅
const formatNewsDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = String(date.getFullYear()).slice(-2)
  return `${day}.${month}.${year}`
}
</script>

<style scoped>
.media {
  display: flex;
  background-color: white;
  padding-top: 6rem;
  height: calc(100vh - 3.75rem);
  overflow: hidden;
}

/* 왼쪽 네비게이션 */
.media-nav {
  width: 34.375rem;
  padding: 2rem 2rem;
  background-color: white;
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
  font-weight: 500;
  letter-spacing: 0.12em;
  color: #FBCE7B;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
}

.nav-item.main-title {
  cursor: pointer;
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
}

.nav-item.main-title h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 500;
  letter-spacing: 0.12em;
  color: #FBCE7B;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
}

.nav-item:hover {
  color: #F9D89C;
}

.nav-item.active {
  color: #FBCE7B;
}

/* 메인 콘텐츠 영역 */
.media-content {
  flex: 1;
  padding: 2rem 2rem 2rem 1rem;
  background-color: white;
  min-height: 0; /* flex 컨테이너 내부 스크롤 허용 */
  overflow-y: auto;
}

.content-section {
  width: 100%;
}

/* MUSIC 섹션 */
.music-content-wrapper {
  display: flex;
  flex-direction: row;
  gap: 3rem;
  align-items: flex-start;
}

.artist-profile-item {
  text-align: center;
  flex: 0 0 17.5rem;
}

.albums-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  flex: 1;
}

.music-item {
  text-align: center;
}

.music-cover {
  width: 100%;
  max-width: 12.5rem;
  max-height: 12.5rem;
  aspect-ratio: 1;
  overflow: hidden;
  margin-bottom: 1rem;
  border-radius: 0.5rem;
  margin: 0 auto;
}

.music-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.music-title {
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 0.25rem;
  color: #333;
}

.music-artist {
  font-size: 0.9rem;
  color: #666;
}

/* VIDEOS 섹션 */
.video-list {
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.video-item {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.video-embed {
  position: relative;
  width: 100%;
  max-width: 60rem;
  aspect-ratio: 16 / 9;
  background: #000;
}

.video-embed iframe {
  position: absolute;
  inset: 0;
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

.video-info {
  flex: 1;
  padding-top: 1rem;
}

.video-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
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
  gap: 1.5rem;
}

.photo-group-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #FBCE7B;
  text-align: left;
  margin-bottom: 0.5rem;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12.5rem, 1fr));
  gap: 1rem;
}

.photo-item {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease;
  border-radius: 0.5rem;
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
  font-size: 1rem;
  color: #666;
  min-width: 5rem;
  text-align: left;
}

.news-content {
  flex: 1;
  margin-left: 2rem;
}

.news-title {
  font-size: 1.1rem;
  font-weight: bold;
}

.news-expand {
  color: #999;
  transition: all 0.3s ease;
  min-width: 1.875rem;
  text-align: right;
}

.news-expand.expanded {
  transform: rotate(180deg);
}

.news-expand:hover {
  color: #666;
}

/* 상세 정보 스타일 */
.news-details {
  padding: 1.5rem 0 2rem 0;
  padding-left: calc(5rem + 2rem); /* news-date width + margin-left */
  background-color: #f8f8f8;
  border-top: 0.0625rem solid #e0e0e0;
}

.news-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
}

/* EQUIPMENT 섹션 */
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(18.75rem, 1fr));
  gap: 2rem;
  padding: 2rem 0;
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
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  text-align: center;
}
</style>