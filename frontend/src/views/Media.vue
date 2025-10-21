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
        <div class="video-list">
          <div class="video-item" v-for="video in videos" :key="video.id">
            <div class="video-embed">
              <iframe
                :src="video.embedUrl"
                title="YouTube video player"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                referrerpolicy="strict-origin-when-cross-origin"
                allowfullscreen
              ></iframe>
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
            <img src="../imgs/exphoto1.png" alt="BOSS RC-600 LOOP STATION">
          </div>
          <div class="equipment-name">Loopstation RC 600</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

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

// 음악 데이터 (앨범들만)
const musicItems = ref([
  {
    id: 1,
    title: 'Time',
    artist: 'TWIIIINS',
    cover: new URL('../imgs/music/time.png', import.meta.url).href
  },
  {
    id: 2,
    title: 'Common Room',
    artist: 'TWIIIINS',
    cover: new URL('../imgs/music/commonRoom.png', import.meta.url).href
  }
])

// 비디오 데이터
const videos = ref([
  {
    id: 1,
    title: 'TWIIIINS Performance 1',
    embedUrl: 'https://www.youtube.com/embed/7D3tv-8Fmlw?si=J9m58u_Y8H1LPnIj'
  },
  {
    id: 2,
    title: 'TWIIIINS Performance 2',
    embedUrl: 'https://www.youtube.com/embed/QSG4jJmb5mA?si=Hs3oEZhQR3WPRXa4'
  },
  {
    id: 3,
    title: 'TWIIIINS Performance 3',
    embedUrl: 'https://www.youtube.com/embed/qj-EO0di6S4?si=QELDshOJkoC_ON7x'
  },
  {
    id: 4,
    title: 'TWIIIINS Performance 4',
    embedUrl: 'https://www.youtube.com/embed/anb6UtTP-wE?si=dCaJRmd-NTWoEJyA'
  }
])

// 동적 이미지 로딩을 위한 import.meta.glob 사용 (자산 URL로 반환)
const photoModules = import.meta.glob('../imgs/Photos/**/*.{jpg,jpeg,png,JPG,JPEG,PNG}', { as: 'url', eager: true })

// 폴더별로 사진들을 그룹화하는 함수
const groupPhotosByFolder = () => {
  const groups = {}
  
  Object.keys(photoModules).forEach(path => {
    // 경로에서 폴더명 추출 (예: ../imgs/Photos/001 02.08.2025 in Flensburg by Flensburger Hofkultur/filename.jpg)
    const pathParts = path.split('/')
    const folderName = pathParts[pathParts.length - 2] // 폴더명
    
    if (!groups[folderName]) {
      groups[folderName] = []
    }
    
    // 폴더명에서 날짜와 장소 정보 추출
    const folderInfo = folderName.match(/^\d+\s+(.+?)\s+by\s+/)
    const title = folderInfo ? folderInfo[1] : folderName
    
    groups[folderName].push({
      src: photoModules[path],
      alt: `TWIIIINS Performance ${title}`,
      folderName: folderName,
      title: title
    })
  })
  
  // 그룹을 배열로 변환하고 정렬
  // 폴더명 앞의 번호로 정렬
  const sortedFolderNames = Object.keys(groups).sort((a, b) => {
    const na = parseInt(a.split(' ')[0], 10)
    const nb = parseInt(b.split(' ')[0], 10)
    return na - nb
  })

  return sortedFolderNames.map((folderName, index) => {
    const folderInfo = folderName.match(/^\d+\s+(.+?)\s+by\s+/)
    const title = folderInfo ? folderInfo[1] : folderName
    
    return {
      id: index + 1,
      title: title,
      photos: groups[folderName]
    }
  }).sort((a, b) => a.id - b.id) // 폴더 번호순으로 정렬
}

// 사진 데이터 (동적 로딩)
const photoGroups = ref(groupPhotosByFolder())

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
  padding-top: 6rem;
  height: calc(100vh - 60px);
  overflow: hidden;
}

/* 왼쪽 네비게이션 */
.media-nav {
  width: 550px;
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
  color: #D4AF37;
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
  color: #D4AF37;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
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
  flex: 0 0 280px;
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
  max-width: 200px;
  max-height: 200px;
  aspect-ratio: 1;
  overflow: hidden;
  margin-bottom: 1rem;
  border-radius: 8px;
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
  max-width: 960px;
  aspect-ratio: 16 / 9;
  background: #000;
}

.video-embed iframe {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
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
  color: #D4AF37;
  text-align: left;
  margin-bottom: 0.5rem;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.photo-item {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease;
  border-radius: 8px;
}

.photo-item:hover {
  transform: scale(1.02);
}

.photo-item img {
  width: 100%;
  height: 200px;
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