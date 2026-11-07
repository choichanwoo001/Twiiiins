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
      <AdminRequestStatus />
      <!-- Dashboard -->
      <DashboardSection v-if="activeSection === 'dashboard'" @navigate="setSection" />

      <!-- Concerts 관리 -->
      <ConcertAdmin 
        v-if="activeSection === 'concerts'"
      />

      <!-- Media 관리 -->
      <MediaAdmin v-if="activeSection === 'media'" @edit-newsletter="openNewsletter" />

      <section v-if="activeSection === 'newsletter'" class="content-section">
        <h1 class="section-title">뉴스레터</h1>
        <NewsletterAdmin :initial-news-id="selectedNewsletterId" :initial-section="newsletterSection" />
      </section>

      <section v-show="activeSection === 'translations'" class="content-section">
        <KeepAlive>
          <TranslationAdmin v-if="activeSection === 'translations'" />
        </KeepAlive>
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
import AdminRequestStatus from '../components/admin/common/AdminRequestStatus.vue'
import { ref } from 'vue'
import { useAppStore } from '../stores/useAppStore'
import AdminSidebar from '../components/admin/AdminSidebar.vue'
import DashboardSection from '../components/admin/DashboardSection.vue'
import ConcertAdmin from '../components/admin/ConcertAdmin.vue'
import MediaAdmin from '../components/admin/MediaAdmin.vue'
import NewsletterAdmin from '../components/admin/NewsletterAdmin.vue'
import ProjectAdmin from '../components/admin/ProjectAdmin.vue'
import ContactAdmin from '../components/admin/ContactAdmin.vue'
import DownloadFilesAdmin from '../components/admin/DownloadFilesAdmin.vue'
import TranslationAdmin from '../components/admin/TranslationAdmin.vue'

// 스토어 사용
const appStore = useAppStore()

// Reactive data
const activeSection = ref('dashboard')
const selectedNewsletterId = ref(null)
const newsletterSection = ref('news')
function setSection(section, newsletterTab = 'news') {
  selectedNewsletterId.value = null
  newsletterSection.value = newsletterTab
  activeSection.value = section
}
function openNewsletter(id) {
  selectedNewsletterId.value = id
  newsletterSection.value = 'news'
  activeSection.value = 'newsletter'
}
</script>

<style scoped>
.admin {
  --admin-ink: #132a41;
  --admin-muted: #475d73;
  --admin-canvas: #eff4f8;
  --admin-surface: #fff;
  --admin-line: #cbd9e4;
  --admin-accent: #245880;
  --admin-tint: #e1edf6;
  display: flex;
  min-height: 100vh;
  background: var(--admin-canvas);
  position: relative;
}

.admin-content {
  min-width: 0;
  flex: 1;
  margin-left: 15rem;
  padding: 0;
}

.content-section {
  padding: clamp(1rem, 3vw, 2.5rem);
  max-width: 96rem;
  margin-inline: auto;
}

.section-title {
  font-size: clamp(1.65rem, 3vw, 2.25rem);
  margin: 0 0 1rem;
  color: var(--admin-ink);
}

.admin :deep(:focus-visible) {
  outline: 2px solid #4b85af;
  outline-offset: 3px;
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
