<template>
  <div class="media">
    <!-- 왼쪽 네비게이션 -->
    <div class="media-nav">
      <div class="nav-item" 
           :class="{ active: activeSection === 'music' }"
           @click="setActiveSection('music')">
        MUSIC
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'videos' }"
           @click="setActiveSection('videos')">
        VIDEOS
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'photos' }"
           @click="setActiveSection('photos')">
        PHOTOS
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'news' }"
           @click="setActiveSection('news')">
        NEWS
      </div>
      <div class="nav-item" 
           :class="{ active: activeSection === 'equipment' }"
           @click="setActiveSection('equipment')">
        EQUIPMENT
      </div>
    </div>

    <!-- 메인 콘텐츠 영역 -->
    <div class="media-content">
      <!-- MUSIC 섹션 -->
      <div v-if="activeSection === 'music'" class="content-section">
        <div class="music-grid">
          <div class="album-item" v-for="album in albums" :key="album.id">
            <div class="album-cover">
              <img :src="album.cover" :alt="album.title">
            </div>
            <div class="album-info">
              <div class="album-title">{{ album.title }}</div>
              <div class="album-artist">TWIIIINS</div>
            </div>
          </div>
        </div>
      </div>

      <!-- VIDEOS 섹션 -->
      <div v-if="activeSection === 'videos'" class="content-section">
        <div class="video-list">
          <div class="video-item" v-for="video in videos" :key="video.id">
            <div class="video-thumbnail">
              <img :src="video.thumbnail" :alt="video.title">
              <div class="play-button">▶</div>
            </div>
            <div class="video-info">
              <div class="video-title">{{ video.title }}</div>
              <div class="video-channel">TWIIIINS</div>
            </div>
          </div>
        </div>
      </div>

      <!-- PHOTOS 섹션 -->
      <div v-if="activeSection === 'photos'" class="content-section">
        <div class="photos-masonry">
          <div class="photo-item" 
               v-for="photo in photos" 
               :key="photo.id"
               :class="photo.size">
            <img :src="photo.src" :alt="photo.alt">
          </div>
        </div>
      </div>

      <!-- NEWS 섹션 -->
      <div v-if="activeSection === 'news'" class="content-section">
        <div class="news-list">
          <div class="news-item" v-for="news in newsList" :key="news.id">
            <div class="news-date">{{ news.date }}</div>
            <div class="news-content">
              <div class="news-title">{{ news.title }}</div>
              <div class="news-description">{{ news.description }}</div>
            </div>
            <div class="news-expand">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>

      <!-- EQUIPMENT 섹션 -->
      <div v-if="activeSection === 'equipment'" class="content-section">
        <div class="equipment-item">
          <div class="equipment-image">
            <img src="../imgs/boss-rc600.png" alt="BOSS RC-600 LOOP STATION">
          </div>
          <div class="equipment-name">Loopstation RC 600</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 활성 섹션 상태
const activeSection = ref('music')

// 섹션 변경 함수
const setActiveSection = (section) => {
  activeSection.value = section
}

// 앨범 데이터
const albums = ref([
  {
    id: 1,
    title: 'Time',
    cover: '../imgs/TwinsMusicApril2024-6 3.png'
  },
  {
    id: 2,
    title: 'Common Room',
    cover: '../imgs/20250719-P1250713 1.png'
  },
  {
    id: 3,
    title: 'Time',
    cover: '../imgs/TwinsMusicApril2024-6 1.png'
  },
  {
    id: 4,
    title: 'Common Room',
    cover: '../imgs/20250719-P1250816 1.png'
  },
  {
    id: 5,
    title: 'Time',
    cover: '../imgs/TwinsMusicApril2024-9 1.png'
  },
  {
    id: 6,
    title: 'Common Room',
    cover: '../imgs/unnamed 1.png'
  },
  {
    id: 7,
    title: 'Time',
    cover: '../imgs/20250719-P1250891 1.png'
  },
  {
    id: 8,
    title: 'Common Room',
    cover: '../imgs/TwinsMusicApril2024-6 3.png'
  },
  {
    id: 9,
    title: 'Time',
    cover: '../imgs/20250719-P1250713 1.png'
  },
  {
    id: 10,
    title: 'Common Room',
    cover: '../imgs/TwinsMusicApril2024-6 1.png'
  }
])

// 비디오 데이터
const videos = ref([
  {
    id: 1,
    title: 'Time - TWIIIINS',
    thumbnail: '../imgs/TwinsMusicApril2024-6 3.png',
    url: 'https://youtube.com/watch?v=example1'
  },
  {
    id: 2,
    title: '[Les Tecchler] Cover by TWIIIINS',
    thumbnail: '../imgs/20250719-P1250713 1.png',
    url: 'https://youtube.com/watch?v=example2'
  }
])

// 사진 데이터 (크기별로 분류)
const photos = ref([
  {
    id: 1,
    src: '../imgs/20250719-P1250713 1.png',
    alt: 'TWIIIINS Performance',
    size: 'large'
  },
  {
    id: 2,
    src: '../imgs/20250719-P1250816 1.png',
    alt: 'TWIIIINS Performance',
    size: 'medium'
  },
  {
    id: 3,
    src: '../imgs/20250719-P1250891 1.png',
    alt: 'TWIIIINS Performance',
    size: 'small'
  },
  {
    id: 4,
    src: '../imgs/TwinsMusicApril2024-6 1.png',
    alt: 'TWIIIINS Performance',
    size: 'large'
  },
  {
    id: 5,
    src: '../imgs/TwinsMusicApril2024-9 1.png',
    alt: 'TWIIIINS Performance',
    size: 'medium'
  },
  {
    id: 6,
    src: '../imgs/unnamed 1.png',
    alt: 'TWIIIINS Performance',
    size: 'small'
  },
  {
    id: 7,
    src: '../imgs/TwinsMusicApril2024-6 3.png',
    alt: 'TWIIIINS Performance',
    size: 'large'
  },
  {
    id: 8,
    src: '../imgs/20250719-P1250713 1.png',
    alt: 'TWIIIINS Performance',
    size: 'medium'
  },
  {
    id: 9,
    src: '../imgs/20250719-P1250816 1.png',
    alt: 'TWIIIINS Performance',
    size: 'small'
  }
])

// 뉴스 데이터
const newsList = ref([
  {
    id: 1,
    date: '15.09.25',
    title: 'Concert announcement',
    description: "We're thrilled to announce that we are returning to Japan for the third time! This time, we'll be performing in Tokyo, Niigata, Nagoya, Sendai, Hyogo, and Yokohama - and we couldn't be"
  },
  {
    id: 2,
    date: '15.09.25',
    title: 'Concert announcement',
    description: "We're thrilled to announce that we are returning to Japan for the third time! This time, we'll be performing in Tokyo, Niigata, Nagoya, Sendai, Hyogo, and Yokohama - and we couldn't be"
  },
  {
    id: 3,
    date: '15.09.25',
    title: 'Concert announcement',
    description: "We're thrilled to announce that we are returning to Japan for the third time! This time, we'll be performing in Tokyo, Niigata, Nagoya, Sendai, Hyogo, and Yokohama - and we couldn't be"
  },
  {
    id: 4,
    date: '15.09.25',
    title: 'Concert announcement',
    description: "We're thrilled to announce that we are returning to Japan for the third time! This time, we'll be performing in Tokyo, Niigata, Nagoya, Sendai, Hyogo, and Yokohama - and we couldn't be"
  }
])
</script>

<style scoped>
.media {
  display: flex;
  background-color: white;
  min-height: calc(100vh - 60px);
  margin-top: -60px;
  padding-top: 60px;
}

/* 왼쪽 네비게이션 */
.media-nav {
  width: 300px;
  padding: 4rem 2rem;
  background-color: white;
}

.nav-item {
  font-size: 2rem;
  font-weight: bold;
  color: #F0C880;
  margin-bottom: 2rem;
  cursor: pointer;
  transition: color 0.3s ease;
  text-align: left;
}

.nav-item:hover {
  color: #E6B800;
}

.nav-item.active {
  color: #D4AF37;
}

/* 메인 콘텐츠 영역 */
.media-content {
  flex: 1;
  padding: 4rem 2rem;
  background-color: white;
}

.content-section {
  width: 100%;
}

/* MUSIC 섹션 */
.music-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 2rem;
}

.album-item {
  text-align: center;
}

.album-cover {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-title {
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.album-artist {
  font-size: 0.9rem;
  color: #666;
}

/* VIDEOS 섹션 */
.video-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.video-item {
  display: flex;
  gap: 1rem;
}

.video-thumbnail {
  position: relative;
  width: 320px;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
}

.video-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 0, 0, 0.8);
  color: white;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
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

/* PHOTOS 섹션 - Masonry 그리드 */
.photos-masonry {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  grid-auto-rows: 10px;
}

.photo-item {
  overflow: hidden;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.photo-item:hover {
  transform: scale(1.02);
}

.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 사진 크기별 그리드 설정 */
.photo-item.small {
  grid-row-end: span 15;
}

.photo-item.medium {
  grid-row-end: span 25;
}

.photo-item.large {
  grid-row-end: span 35;
}

/* NEWS 섹션 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.news-item {
  display: flex;
  align-items: center;
  padding: 1.5rem 0;
  border-bottom: 1px solid #e0e0e0;
}

.news-item:last-child {
  border-bottom: none;
}

.news-date {
  font-size: 1rem;
  color: #666;
  min-width: 80px;
  text-align: left;
}

.news-content {
  flex: 1;
  margin-left: 2rem;
}

.news-title {
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.news-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
}

.news-expand {
  color: #999;
  cursor: pointer;
  min-width: 30px;
  text-align: right;
}

/* EQUIPMENT 섹션 */
.equipment-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.equipment-image {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
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

/* 반응형 디자인 */
@media (max-width: 768px) {
  .media {
    flex-direction: column;
  }
  
  .media-nav {
    width: 100%;
    padding: 2rem 1rem;
    display: flex;
    gap: 1rem;
    overflow-x: auto;
  }
  
  .nav-item {
    font-size: 1.2rem;
    margin-bottom: 0;
    white-space: nowrap;
  }
  
  .media-content {
    padding: 2rem 1rem;
  }
  
  .music-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 1rem;
  }
  
  .video-item {
    flex-direction: column;
  }
  
  .video-thumbnail {
    width: 100%;
    height: 200px;
  }
  
  .photos-masonry {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
}
</style>
