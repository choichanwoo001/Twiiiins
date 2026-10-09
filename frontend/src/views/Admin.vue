<template>
  <div class="admin">
    <!-- 업로드 진행률 표시 -->
    <div v-if="appStore.isUploading" class="upload-progress-overlay">
      <div class="upload-progress-container">
        <div class="upload-progress-bar">
          <div 
            class="upload-progress-fill" 
            :style="{ width: appStore.uploadProgress + '%' }"
          ></div>
        </div>
        <div class="upload-progress-text">
          업로드 중... {{ appStore.uploadProgress }}%
        </div>
      </div>
    </div>

    <!-- 사이드바 컴포넌트 -->
    <AdminSidebar 
      :active-section="activeSection"
      @update:active-section="setSection"
    />

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
      />

      <!-- Media 관리 -->
      <MediaAdmin v-if="activeSection === 'media'" @edit-newsletter="openNewsletter" />

      <section v-if="activeSection === 'newsletter'" class="content-section">
        <h1 class="section-title">Newsletter 관리</h1>
        <NewsletterAdmin :initial-news-id="selectedNewsletterId" />
      </section>

      <!-- Projects 관리 -->
      <ProjectAdmin v-if="activeSection === 'projects'" />

      <!-- Contact 관리 -->
      <ContactAdmin 
        v-if="activeSection === 'contact'"
      />

      <!-- Download Files 관리 -->
      <DownloadFilesAdmin 
        v-if="activeSection === 'download-files'"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useConcertStore } from '../stores/useConcertStore'
import { useMediaStore } from '../stores/useMediaStore'
import { useAppStore } from '../stores/useAppStore'
import AdminSidebar from '../components/admin/AdminSidebar.vue'
import DashboardSection from '../components/admin/DashboardSection.vue'
import ConcertAdmin from '../components/admin/ConcertAdmin.vue'
import MediaAdmin from '../components/admin/MediaAdmin.vue'
import NewsletterAdmin from '../components/admin/NewsletterAdmin.vue'
import ProjectAdmin from '../components/admin/ProjectAdmin.vue'
import ContactAdmin from '../components/admin/ContactAdmin.vue'
import DownloadFilesAdmin from '../components/admin/DownloadFilesAdmin.vue'

// 스토어 사용
const concertStore = useConcertStore()
const mediaStore = useMediaStore()
const appStore = useAppStore()

// Reactive data
const activeSection = ref('dashboard')
const selectedNewsletterId = ref(null)
function setSection(section) { selectedNewsletterId.value = null; activeSection.value = section }
function openNewsletter(id) { selectedNewsletterId.value = id; activeSection.value = 'newsletter' }

// Dashboard data (스토어에서 가져온 데이터)
const concerts = computed(() => concertStore.concerts)
const photoGroups = computed(() => mediaStore.photoGroups)
const musicList = computed(() => mediaStore.musicItems)
const videos = computed(() => mediaStore.videos)
const newsList = computed(() => mediaStore.newsItems)
const equipmentList = computed(() => mediaStore.equipmentItems)

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
  position: relative;
}

.admin-content {
  min-width: 0;
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
  color: #1E1D1D;
}

/* 업로드 진행률 오버레이 */
.upload-progress-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.upload-progress-container {
  max-width: 30rem;
  margin: 0 auto;
}

.upload-progress-bar {
  width: 100%;
  height: 0.5rem;
  background: #e0e0e0;
  border-radius: 0.25rem;
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.upload-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #45a049);
  border-radius: 0.25rem;
  transition: width 0.3s ease;
  box-shadow: 0 0 0.5rem rgba(76, 175, 80, 0.5);
}

.upload-progress-text {
  text-align: center;
  font-size: 0.875rem;
  color: #666;
  font-weight: 500;
}
@media (max-width: 768px) {
  .admin { display: block; }
  .admin-content { margin-left: 0; min-width: 0; width: 100%; }
}
</style>
