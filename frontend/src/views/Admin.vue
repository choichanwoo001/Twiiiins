<template>
  <div class="admin">
    <!-- 왼쪽 사이드바 -->
    <aside class="admin-sidebar" role="navigation" aria-label="관리자 메뉴">
      <h2 class="sidebar-title">ADMIN</h2>
      <nav class="sidebar-nav" role="tablist">
        <button 
          :class="['nav-item', { active: activeSection === 'dashboard' }]"
          @click="activeSection = 'dashboard'"
          role="tab"
          :aria-selected="activeSection === 'dashboard'"
          aria-label="대시보드 메뉴">
          Dashboard
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'concerts' }]"
          @click="activeSection = 'concerts'"
          role="tab"
          :aria-selected="activeSection === 'concerts'"
          aria-label="콘서트 관리 메뉴">
          Concerts
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'media' }]"
          @click="activeSection = 'media'"
          role="tab"
          :aria-selected="activeSection === 'media'"
          aria-label="미디어 관리 메뉴">
          Media
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'contact' }]"
          @click="activeSection = 'contact'"
          role="tab"
          :aria-selected="activeSection === 'contact'"
          aria-label="연락처 관리 메뉴">
          Contact
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'download-files' }]"
          @click="activeSection = 'download-files'"
          role="tab"
          :aria-selected="activeSection === 'download-files'"
          aria-label="다운로드 파일 관리 메뉴">
          Download Files
        </button>
      </nav>
    </aside>

    <!-- 메인 콘텐츠 영역 -->
    <main class="admin-content" role="tabpanel" :aria-label="`${activeSection} 관리 섹션`">
      <!-- Dashboard -->
      <DashboardSection 
        v-if="activeSection === 'dashboard'"
        :concerts="concerts"
        :photo-groups="photoGroups"
        :music-list="musicList"
        :videos="videos"
        :news-list="newsList"
        :equipment-list="equipmentList"
        @navigate="activeSection = $event"
      />

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
import { ref, onMounted, computed } from 'vue'
import { useConcertStore, useMediaStore, useAppStore } from '../stores'
import DashboardSection from '../components/admin/DashboardSection.vue'
import ConcertAdmin from '../components/admin/ConcertAdmin.vue'
import MediaAdmin from '../components/admin/MediaAdmin.vue'
import ContactAdmin from '../components/admin/ContactAdmin.vue'
import DownloadFilesAdmin from '../components/admin/DownloadFilesAdmin.vue'

// 스토어 사용
const concertStore = useConcertStore()
const mediaStore = useMediaStore()
const appStore = useAppStore()

// Reactive data
const activeSection = ref('dashboard')

// Dashboard data (스토어에서 가져온 데이터)
const concerts = computed(() => concertStore.concerts)
const photoGroups = computed(() => mediaStore.photoGroups)
const musicList = computed(() => mediaStore.musicItems)
const videos = computed(() => mediaStore.videos)
const newsList = ref([])
const equipmentList = ref([])
const contacts = ref([])
const downloadFiles = ref([])

// Methods
const loadDashboardData = async () => {
  try {
    appStore.setLoading(true)
    // 스토어를 통해 데이터 로드
    await Promise.all([
      concertStore.loadConcerts(),
      mediaStore.loadAllMedia()
    ])
  } catch (error) {
    appStore.setError('대시보드 데이터 로드에 실패했습니다.')
    console.error('대시보드 데이터 로드 실패:', error)
  } finally {
    appStore.setLoading(false)
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

</style>