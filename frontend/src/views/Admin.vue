<template>
  <div class="admin">
    <!-- 사이드바 컴포넌트 -->
    <AdminSidebar 
      :active-section="activeSection"
      @update:active-section="activeSection = $event"
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
      <MediaAdmin v-if="activeSection === 'media'" />

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
import { useConcertStore, useMediaStore, useAppStore } from '../stores'
import AdminSidebar from '../components/admin/AdminSidebar.vue'
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
const newsList = computed(() => mediaStore.newsItems)
const equipmentList = computed(() => mediaStore.equipmentItems)
const contacts = ref([]) // Contact 스토어가 있다면 사용
const downloadFiles = ref([]) // DownloadFiles 스토어가 있다면 사용

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