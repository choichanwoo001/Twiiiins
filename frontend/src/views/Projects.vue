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
              loading="lazy"
              @error="handleImageError($event)"
            />
            <div v-else class="image-error">
              Image not found
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
        <p>No projects found.</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { projectService } from '../services'
import { formatDate, toAbsoluteUrl } from '../utils/commonHelpers'
import { logError } from '../utils/errorHandler'

const router = useRouter()

// 프로젝트 목록
const projects = ref([])

// 프로젝트 데이터 로드
const loadProjects = async () => {
  try {
    const projectData = await projectService.getAllProjects()
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
  errorDiv.textContent = 'Image not found'
  img.parentElement.appendChild(errorDiv)
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
/* 레이아웃 */
.projects {
  background: var(--color-background); /* #fff */
  color: var(--color-text); /* #222 -> var(--color-text) */
  padding-top: 3rem;
  display:grid;
  grid-template-columns: 1fr 2.2fr;
  align-items: start;
  gap: 4rem;
  margin-left: auto;
  margin-right: auto;
  padding-left: 2rem;
  padding-right: 4rem;
  height: calc(100vh - var(--navbar-height));
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
  color: #5B8793; /* This seems specific, keeping it or should I add a var? */
  text-transform: uppercase;
  line-height:1;
  margin:0;
}

/* 우측 콘텐츠 */
.projects-content{
  max-width: 64rem; /* 이미지 크기 확장을 위해 max-width 증가 */
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
  gap: 3.5rem; /* 간격 증가 */
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
  aspect-ratio: 2.39 / 1;        /* 시네마틱 비율 (2.39:1) */
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
  align-items: flex-end; /* 텍스트 베이스라인 정렬 */
  justify-content: space-between;
  gap: 1rem;
  margin-top: 1rem;
  padding: 0;
}
.caption-left{
  font-size: 1.125rem; /* 18px */
  letter-spacing: 0.02em;
  color: var(--color-text); /* #1E1D1D */
  font-weight: 600;
  text-transform: uppercase; /* 대문자 강제 */
  line-height: 1.2;
}
.caption-right{
  display:flex;
  align-items:center;
  gap: 0.25rem; /* 간격 미세 조정 */
  font-size: 0.8125rem; /* 13px */
  color: var(--color-text-lighter); /* #888 */
  font-weight: 400;
  transition: color 0.2s ease;
  line-height: 1.2;
  margin-bottom: 0.1rem; /* 시각적 정렬 보정 */
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
  margin-right: 0.2rem;
}

.caption-arrow{
  flex:0 0 auto;
  color:inherit;
  transition: transform 0.2s ease, color 0.2s ease;
  margin-left: 0.5rem;
  width: 0.875rem; /* 화살표 크기 조정 */
  height: 0.875rem;
  opacity: 0.7;
}

.project-item:hover .caption-arrow {
  transform: translate(2px, -2px); /* 대각선 이동 효과 */
  opacity: 1;
  color: var(--color-text); /* #1E1D1D */
}

/* 호버 미세 인터랙션(선택) */
.project-figure:hover img{
  transform: scale(1.02); /* 확대 비율 미세 조정 */
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 20rem;
  color: #999;
  font-size: 1.2rem;
}


/* 모바일 최적화 (768px 이하) */
@media (max-width: 768px) {
  .projects {
    display: block;
    height: auto;
    overflow: visible;
    padding-top: 4rem; /* 헤더 공간 + 여백 */
    padding-left: 1.5rem;
    padding-right: 1.5rem;
    padding-bottom: 2rem;
  }

  .projects-title {
    padding-top: 0;
    margin-bottom: 4rem;
  }

  .projects-title h1 {
    font-size: 2.5rem; /* 모바일에서 타이틀 크기 조정 */
  }

  .projects-content {
    width: 100%;
    margin-right: 0;
    padding-top: 0;
    overflow-y: visible;
  }
 
  .projects-list {
    gap: 4rem; /* 프로젝트 간격 */
    padding-bottom: 0;
  }

  /* .project-figure {
    모바일에서는 비율을 조금 더 높게(덜 와이드하게) 가져갈 수도 있음. 
       일단 유지하거나 필요시 aspect-ratio 조정
  } */

  .project-caption {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  /* 모바일에서 제목과 날짜/장소 배치를 스크린샷처럼 조정
     스크린샷:
     ARTURO UI           02.22.2025, Salzburg State Theater (arrow)
     
     현재 구조:
     .project-caption -> flex row (desktop)
       .caption-left (Title)
       .caption-right (Date, Location, Arrow)
     
     모바일에서도 한 줄에 나올 수 있으면 한 줄 유지, 
     공간 부족시 줄바꿈. 하지만 스크린샷은 한 줄로 보임.
     Flex wrap을 허용하거나, 폰트 사이즈를 줄여서 한 줄 유지.
  */
  .project-caption {
    flex-direction: row; /* 다시 row로 */
    align-items: flex-start; /* 상단 정렬 */
    justify-content: space-between;
  }

  .caption-left {
    font-size: 1rem; /* 제목 크기 약간 축소 */
    max-width: 50%; /* 제목이 너무 길어지면 줄바꿈 되도록 */
    word-break: keep-all;
  }

  .caption-right {
    font-size: 0.75rem; /* 부가 정보 크기 축소 */
    text-align: right;
    justify-content: flex-end;
    flex: 1;
    margin-top: 0.1rem; /* 시각적 줄맞춤 */
  }
}
</style>
