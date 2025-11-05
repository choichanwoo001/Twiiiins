<template>
  <div class="projects">
    <!-- 좌측 고정 타이틀 -->
    <aside class="projects-title">
      <h1>PROJECTS</h1>
    </aside>

    <!-- 우측 콘텐츠 -->
    <main class="projects-content">
      <!-- 프로젝트 목록 -->
      <div v-if="projects.length > 0" class="projects-list">
        <div 
          v-for="project in projects" 
          :key="project.id" 
          class="project-item"
        >
          <!-- 이미지 -->
          <figure 
            class="project-figure"
            :class="{ 'clickable': project.urlSlug }"
            @click="project.urlSlug ? goToProjectDetail(project.urlSlug) : null"
          >
            <img
              v-if="project.coverImageUrl"
              :src="toAbsoluteUrl(project.coverImageUrl)"
              :alt="project.title || 'Project image'"
              @error="handleImageError($event)"
            />
            <div v-else class="image-error">
              이미지를 불러올 수 없습니다
            </div>
          </figure>

          <!-- 캡션 행 -->
          <div class="project-caption">
            <div class="caption-left">{{ project.title }}</div>
            <div 
              class="caption-right" 
              v-if="project.urlSlug"
            >
              <span 
                class="caption-date"
                @click.stop="goToProjectDetail(project.urlSlug)"
              >{{ formatDate(project.premiereDate, 'short', 'en-US') }}</span>
              <span class="caption-separator">, </span>
              <span 
                class="caption-location"
                @click.stop="goToProjectDetail(project.urlSlug)"
              >{{ project.location }}</span>
              <svg class="caption-arrow" width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M7 17L17 7M17 7H9M17 7V15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="caption-right" v-else>
              <span>{{ formatDate(project.premiereDate, 'short', 'en-US') }}, {{ project.location }}</span>
            </div>
          </div> 
        </div>
      </div>

      <!-- 프로젝트가 없을 때 -->
      <div v-else class="empty-state">
        <p>등록된 프로젝트가 없습니다.</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../api/axios'
import { formatDate, toAbsoluteUrl } from '../utils/commonHelpers'
import { logError } from '../utils/errorHandler'

const router = useRouter()

// 프로젝트 목록
const projects = ref([])

// 프로젝트 데이터 로드
const loadProjects = async () => {
  try {
    const response = await axios.get('/projects')
    const projectData = response.data.data || response.data || []
    
    // 원본 데이터를 그대로 저장 (News와 동일한 방식)
    projects.value = projectData
  } catch (error) {
    logError(error, '프로젝트 데이터 로드')
    projects.value = []
  }
}

// 프로젝트 상세로 이동
const goToProjectDetail = (urlSlug) => {
  if (urlSlug) {
    router.push(`/projects/${urlSlug}`)
  }
}

// 이미지 로드 에러 처리
const handleImageError = (event) => {
  // 이미지 로드 실패 시 이미지를 숨기고 에러 메시지 표시
  const img = event.target
  img.style.display = 'none'
  const errorDiv = document.createElement('div')
  errorDiv.className = 'image-error'
  errorDiv.textContent = '이미지를 불러올 수 없습니다'
  img.parentElement.appendChild(errorDiv)
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
/* 레이아웃 */
.projects {
  background:#fff;
  color:#222;
  padding-top: 6rem;
  display:grid;
  grid-template-columns: 1fr 2.2fr;
  align-items: start;
  gap: 4rem;
  margin-left: auto;
  margin-right: auto;
  padding-left: 2rem;
  padding-right: 4rem;
  height: calc(100vh - 3.75rem);
  overflow: hidden;
}

/* 좌측 타이틀 */
.projects-title {
  display:flex;
  align-items:flex-start;
  justify-content:flex-start;
  padding-top: 2rem;
}
.projects-title h1{
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color:#5B8793;
  text-transform: uppercase;
  line-height:1;
  margin:0;
}

/* 우측 콘텐츠 */
.projects-content{
  max-width: 50rem; /* 이미지 크기 줄임 */
  width: 100%;
  margin-right: 2rem;
  justify-self: end;           /* 우측 정렬 느낌 */
  padding-top: 2rem; /* 콘텐츠를 아래로 이동 */
  min-height: 0; /* 그리드 아이템 내부 스크롤 허용 */
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-gutter: stable;
  box-sizing: border-box;
}

/* 프로젝트 목록 */
.projects-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding-bottom: 4rem;
}

.project-item {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.project-item:hover {
  transform: translateY(-0.125rem);
}

/* 이미지 컨테이너 */
.project-figure{
  width:100%;
  aspect-ratio: 16 / 9;        /* 가로형 파노라마 비율로 조정 */
  background:#eee;
  overflow:hidden;
  margin: 0;                   /* figure 기본 마진 제거 */
  border: 0;
}

.project-figure.clickable {
  cursor: pointer;
}
.project-figure img{
  width:100%;
  height:100%;
  object-fit: cover;
  display:block;
  transform: translateZ(0);
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
  font-size: 0.9rem;
  text-align: center;
  padding: 1rem;
}

/* 캡션 행 */
.project-caption{
  display:flex;
  align-items:center;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 0.75rem;
  padding: 0 0.25rem;
}
.caption-left{
  font-size: 0.98rem;
  letter-spacing: 0.04em;
  color:#222;
}
.caption-right{
  display:flex;
  align-items:center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color:#666;
  transition: color 0.2s ease;
}

.caption-date,
.caption-location {
  cursor: pointer;
  transition: color 0.2s ease;
}

.caption-date:hover,
.caption-location:hover {
  color: #1E1D1D;
}

.caption-separator {
  cursor: default;
}

.caption-arrow{
  flex:0 0 auto;
  color:inherit;
  transition: color 0.2s ease;
}

/* 호버 미세 인터랙션(선택) */
.project-figure:hover img{
  transform: scale(1.01);
  transition: transform .25s ease;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 20rem;
  color: #999;
  font-size: 1.2rem;
}

</style>
