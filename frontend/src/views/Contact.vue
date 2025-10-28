<template>
  <div class="contact">
    <!-- 좌측 고정 타이틀 -->
    <aside class="contact-title">
      <h1>CONTACT</h1>
    </aside>

    <!-- 우측 콘텐츠 -->
    <main class="contact-content">
      <!-- 안내 문구 -->
      <div class="contact-info">
        <p class="intro-text">
          FOR COLLABORATIONS, PERFORMANCE INQUIRIES, OR PORTFOLIO REQUESTS,<br>
          PLEASE CONTACT US HERE. WE LOOK FORWARD TO CONNECTING WITH YOU!
        </p>
      </div>

      <!-- 연락처 + 다운로드 병렬 섹션 -->
      <div class="contact-body">
        <!-- 연락처 정보 -->
        <div class="contact-details">
          <div v-for="contact in contacts" :key="contact.id" class="contact-item">
            <div class="contact-name">{{ contact.name }}</div>
            <div v-if="contact.role" class="contact-role">{{ contact.role }}</div>
            <div class="contact-email">{{ contact.email }}</div>
          </div>
        </div>

        <!-- 다운로드 섹션 -->
        <div class="download-section">
          <h2>Download</h2>
          <div class="download-list">
            <div v-for="file in downloadFiles" :key="file.id" class="download-item" @click="downloadFile(file.fileUrl, file.name)">
              <span>{{ file.name }}</span>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 5V19M12 19L7 14M12 19L17 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../api/axios'

// 백엔드 절대 URL 생성 유틸
const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
const toAbsoluteUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url
  return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
}

// 데이터
const contacts = ref([])
const downloadFiles = ref([])

// 연락처 데이터 로드
const loadContacts = async () => {
  try {
    const response = await axios.get('/api/media/contacts')
    contacts.value = response.data.data || response.data
  } catch (error) {
    // 연락처 로드 실패
  }
}

// 다운로드 파일 데이터 로드
const loadDownloadFiles = async () => {
  try {
    const response = await axios.get('/api/media/download-files')
    const files = response.data.data || response.data
    // displayOrder 순으로 정렬
    downloadFiles.value = files.sort((a, b) => (a.displayOrder || 0) - (b.displayOrder || 0))
  } catch (error) {
    // 다운로드 파일 로드 실패
  }
}

// 파일 다운로드
const downloadFile = (fileUrl, fileName) => {
  const absoluteUrl = toAbsoluteUrl(fileUrl)
  const link = document.createElement('a')
  link.href = absoluteUrl
  link.download = fileName
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 페이지 로드 시 데이터 가져오기
onMounted(() => {
  loadContacts()
  loadDownloadFiles()
})
</script>

<style scoped>
/* 레이아웃 */
.contact {
  background: #fff;
  color: #222;
  padding-top: 6rem;
  display: grid;
  grid-template-columns: 1fr 2.2fr;
  align-items: start;
  gap: 4rem;
  margin-left: auto;
  margin-right: auto;
  padding-left: 2rem;
  padding-right: 4rem;
}

/* 좌측 타이틀 */
.contact-title {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding-top: 2rem;
}

.contact-title h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 500;
  letter-spacing: 0.12em;
  color: #815D47;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
}

/* 우측 콘텐츠 */
.contact-content {
  max-width: 50rem;
  width: 100%;
  margin-right: 2rem;
  justify-self: end;
  padding-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

/* 연락처 + 다운로드 병렬 컨테이너 */
.contact-body {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 2rem;
}

/* 안내 문구 */
.contact-info {
  margin-bottom: 2rem;
}

.intro-text {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* 연락처 정보 */
.contact-details {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  flex: 1;
  max-width: 25rem;
}

.contact-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.contact-name {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  line-height: 1;
}

.contact-role {
  font-size: 0.9rem;
  color: #666;
  font-style: italic;
}

.contact-email {
  font-size: 1rem;
  color: #555;
}

/* 다운로드 섹션 */
.download-section {
  margin-top: 0;
  min-width: 15rem;
  max-width: 18.75rem;
}

.download-section h2 {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  margin: 0 0 0.25rem 0;
  line-height: 1;
}

.download-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.download-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: color 0.3s ease;
}

.download-item:hover {
  color: #666;
}

.download-item span {
  font-size: 1rem;
  color: #555;
}

.download-item svg {
  color: #999;
  transition: color 0.3s ease;
}

.download-item:hover svg {
  color: #666;
}

</style>
