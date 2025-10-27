<template>
  <div class="admin">
    <!-- 왼쪽 사이드바 -->
    <aside class="admin-sidebar">
      <h2 class="sidebar-title">ADMIN</h2>
      <nav class="sidebar-nav">
        <button 
          :class="['nav-item', { active: activeSection === 'dashboard' }]"
          @click="activeSection = 'dashboard'">
          Dashboard
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'concerts' }]"
          @click="activeSection = 'concerts'">
          Concerts
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'media' }]"
          @click="activeSection = 'media'">
          Media
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'contact' }]"
          @click="activeSection = 'contact'">
          Contact
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'download-files' }]"
          @click="activeSection = 'download-files'">
          Download Files
        </button>
      </nav>
    </aside>

    <!-- 메인 콘텐츠 영역 -->
    <main class="admin-content">
      <!-- Dashboard -->
      <div v-if="activeSection === 'dashboard'" class="content-section">
        <h1 class="section-title">Dashboard</h1>
        
        <!-- 통계 카드들 -->
        <div class="dashboard-stats">
          <div class="stat-card">
            <div class="stat-icon">🎵</div>
            <div class="stat-content">
              <h3>콘서트</h3>
              <p class="stat-number">{{ concerts.length }}</p>
              <p class="stat-label">총 콘서트 수</p>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon">📸</div>
            <div class="stat-content">
              <h3>사진 그룹</h3>
              <p class="stat-number">{{ photoGroups.length }}</p>
              <p class="stat-label">총 사진 그룹 수</p>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="stat-icon">🎬</div>
            <div class="stat-content">
              <h3>미디어</h3>
              <p class="stat-number">{{ musicList.length + videos.length + newsList.length + equipmentList.length }}</p>
              <p class="stat-label">총 미디어 수</p>
            </div>
          </div>
        </div>

        <!-- 최근 활동 -->
        <div class="dashboard-section">
          <h2>최근 활동</h2>
          <div class="activity-list">
            <div class="activity-item">
              <div class="activity-icon">🎵</div>
              <div class="activity-content">
                <h4>콘서트 관리</h4>
                <p>예정된 콘서트: {{ concerts.filter(c => !c.isPast).length }}개</p>
                <p>지난 콘서트: {{ concerts.filter(c => c.isPast).length }}개</p>
              </div>
            </div>
            
            <div class="activity-item">
              <div class="activity-icon">📸</div>
              <div class="activity-content">
                <h4>사진 관리</h4>
                <p>총 사진 그룹: {{ photoGroups.length }}개</p>
                <p>총 사진 수: {{ photoGroups.reduce((total, group) => total + (group.photos ? group.photos.length : 0), 0) }}개</p>
              </div>
            </div>
            
            <div class="activity-item">
              <div class="activity-icon">🎬</div>
              <div class="activity-content">
                <h4>미디어 관리</h4>
                <p>음악: {{ musicList.length }}개</p>
                <p>비디오: {{ videos.length }}개</p>
                <p>사진 그룹: {{ photoGroups.length }}개</p>
                <p>뉴스: {{ newsList.length }}개</p>
                <p>장비: {{ equipmentList.length }}개</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 빠른 작업 -->
        <div class="dashboard-section">
          <h2>빠른 작업</h2>
          <div class="quick-actions">
            <button class="quick-action-btn" @click="activeSection = 'concerts'">
              <div class="quick-action-icon">🎵</div>
              <span>새 콘서트 추가</span>
            </button>
            
            <button class="quick-action-btn" @click="activeSection = 'media'">
              <div class="quick-action-icon">📸</div>
              <span>사진 업로드</span>
            </button>
            
            <button class="quick-action-btn" @click="activeSection = 'media'">
              <div class="quick-action-icon">🎬</div>
              <span>음악 추가</span>
            </button>
          </div>
        </div>

        <!-- 시스템 상태 -->
        <div class="dashboard-section">
          <h2>시스템 상태</h2>
          <div class="system-status">
            <div class="status-item">
              <span class="status-label">데이터베이스 연결</span>
              <span class="status-indicator success">●</span>
              <span class="status-text">정상</span>
            </div>
            <div class="status-item">
              <span class="status-label">파일 업로드</span>
              <span class="status-indicator success">●</span>
              <span class="status-text">정상</span>
            </div>
            <div class="status-item">
              <span class="status-label">API 서버</span>
              <span class="status-indicator success">●</span>
              <span class="status-text">정상</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Concerts 관리 -->
      <ConcertAdmin 
        v-if="activeSection === 'concerts'"
        :concerts="concerts"
        @update:concerts="concerts = $event"
      />

      <!-- Media 관리 -->
      <MediaAdmin v-if="activeSection === 'media'" />

      <!-- Contact 관리 -->
      <ContactAdmin 
        v-if="activeSection === 'contact'"
        :contacts="contacts"
        @update:contacts="contacts = $event"
      />

      <!-- Download Files 관리 -->
      <DownloadFilesAdmin 
        v-if="activeSection === 'download-files'"
        :files="downloadFiles"
        @update:files="downloadFiles = $event"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../api/axios'
import ConcertAdmin from '../components/admin/ConcertAdmin.vue'
import MediaAdmin from '../components/admin/MediaAdmin.vue'
import ContactAdmin from '../components/admin/ContactAdmin.vue'
import DownloadFilesAdmin from '../components/admin/DownloadFilesAdmin.vue'

// Reactive data
const activeSection = ref('dashboard')

// Dashboard data
const concerts = ref([])
const photoGroups = ref([])
const musicList = ref([])
const videos = ref([])
const newsList = ref([])
const equipmentList = ref([])
const contacts = ref([])
const downloadFiles = ref([])

// Methods
const loadDashboardData = async () => {
  try {
    const [concertsRes, photoGroupsRes, musicRes, videosRes, newsRes, equipmentRes, contactsRes, downloadFilesRes] = await Promise.all([
      axios.get('/api/concerts'),
      axios.get('/api/media/photo-groups'),
      axios.get('/api/media/music'),
      axios.get('/api/media/videos'),
      axios.get('/api/media/news'),
      axios.get('/api/media/equipment'),
      axios.get('/api/media/contacts'),
      axios.get('/api/media/download-files')
    ])
    
    concerts.value = concertsRes.data
    photoGroups.value = photoGroupsRes.data
    musicList.value = musicRes.data
    videos.value = videosRes.data
    newsList.value = newsRes.data
    equipmentList.value = equipmentRes.data
    contacts.value = contactsRes.data
    downloadFiles.value = downloadFilesRes.data
  } catch (error) {
    console.error('대시보드 데이터 로드 실패:', error)
  }
}

// Lifecycle
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.admin {
  display: flex;
  min-height: 100vh;
  background: #f8f9fa;
}

.admin-sidebar {
  width: 15rem;
  background: #2c3e50;
  color: white;
  padding: 2rem 0;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-title {
  font-size: 1.5rem;
  font-weight: bold;
  text-align: center;
  margin-bottom: 2rem;
  padding: 0 1rem;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
}

.nav-item {
  padding: 1rem 2rem;
  background: none;
  border: none;
  color: #bdc3c7;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1rem;
}

.nav-item:hover {
  background: #34495e;
  color: white;
}

.nav-item.active {
  background: #3498db;
  color: white;
}

.admin-content {
  flex: 1;
  margin-left: 15rem;
  padding: 0;
}

.content-section {
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #333;
}

/* Dashboard Styles */
.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(15rem, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
  width: 4rem;
  height: 4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 50%;
}

.stat-content h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.1rem;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #3498db;
  margin: 0 0 0.25rem 0;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.dashboard-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.dashboard-section h2 {
  margin: 0 0 1.5rem 0;
  color: #333;
  font-size: 1.3rem;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(20rem, 1fr));
  gap: 1.5rem;
}

.activity-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
  min-height: 12rem;
}

.activity-icon {
  font-size: 2rem;
  width: 4rem;
  height: 4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.activity-content h4 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.1rem;
  font-weight: 600;
}

.activity-content p {
  margin: 0.25rem 0;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.4;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
  gap: 1rem;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  color: #333;
}

.quick-action-btn:hover {
  border-color: #3498db;
  background: #f8f9fa;
}

.quick-action-icon {
  font-size: 2rem;
}

.quick-action-btn span {
  font-weight: 500;
  font-size: 0.9rem;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
}

.status-label {
  font-weight: 500;
  color: #333;
  min-width: 8rem;
}

.status-indicator {
  font-size: 1.2rem;
}

.status-indicator.success {
  color: #28a745;
}

.status-text {
  color: #666;
  font-size: 0.9rem;
}
</style>