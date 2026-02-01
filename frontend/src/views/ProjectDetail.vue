<template>
  <div class="project-detail">
    <!-- 텍스트 섹션 (왼쪽 정렬) -->
    <section class="project-text" v-if="project">
      <h1>{{ project.title }}</h1>
      <div class="project-content">
        <div class="project-subtitle">{{ project.subtitle }}</div>
        <div class="project-date-location">Premiere: {{ formatDate(project.premiereDate, 'date', 'en-US') }} · {{ project.location }}</div>
        <div class="project-director" v-if="project.director">Director: {{ project.director }}</div>
        <p v-for="(description, index) in projectDescriptions" :key="index">{{ description }}</p>
        <p v-if="project.thankYouText">{{ project.thankYouText }}</p>
        <div class="more-info" v-if="project.moreInfoUrl">
          <a :href="project.moreInfoUrl" target="_blank">
            More Information →
          </a>
        </div>
      </div>
    </section>

    <!-- 이미지 섹션 -->
    <section class="project-images" v-if="project">
      <ImageGrid :images="projectImageUrls" :alt-text-prefix="project.title" />
    </section>

    <!-- 리뷰 섹션 -->
    <section class="reviews-section" v-if="project && projectReviews.length > 0">
      <h2>REVIEWS</h2>
      <div class="reviews-grid">
        <div class="review-item" v-for="(review, index) in projectReviews" :key="index">
          <blockquote>
            „{{ review.text }}"
          </blockquote>
          <cite>{{ review.source }}</cite>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from '../api/axios'
import { ImageGrid } from '../components/common'
import { formatDate, toAbsoluteUrl } from '../utils/commonHelpers'
import { logError } from '../utils/errorHandler'

const route = useRoute()

const project = ref(null)

// 프로젝트 이미지 URL 배열 생성
const projectImageUrls = computed(() => {
  if (!project.value) return []
  
  if (project.value.imageUrls && Array.isArray(project.value.imageUrls) && project.value.imageUrls.length > 0) {
    return project.value.imageUrls.map(url => toAbsoluteUrl(url))
  }
  
  return []
})

// 프로젝트 리뷰 배열 생성
const projectReviews = computed(() => {
  if (!project.value) return []
  
  if (project.value.reviews && Array.isArray(project.value.reviews) && project.value.reviews.length > 0) {
    return project.value.reviews.filter(review => review.text && review.text.trim() !== '')
  }
  
  return []
})

// 프로젝트 설명 배열 생성
const projectDescriptions = computed(() => {
  if (!project.value) return []
  
  if (project.value.descriptions && Array.isArray(project.value.descriptions) && project.value.descriptions.length > 0) {
    return project.value.descriptions.filter(desc => desc && desc.trim() !== '')
  }
  
  return []
})

// 프로젝트 데이터 로드
const loadProject = async () => {
  const urlSlug = route.params.slug
  
  if (!urlSlug) {
    return
  }
  
  try {
    const response = await axios.get(`/projects/slug/${urlSlug}`)
    const projectData = response.data.data || response.data
    
    if (projectData) {
      project.value = {
        ...projectData,
        // 이미지 URL 변환
        imageUrls: projectData.imageUrls ? projectData.imageUrls.map(url => toAbsoluteUrl(url)) : []
      }
    }
  } catch (error) {
    logError(error, '프로젝트 데이터 로드')
    project.value = null
  }
}

onMounted(async () => {
  await loadProject()
})

</script>

<style scoped>
.project-detail {
  background-color: white;
  color: #1E1D1D;
  font-family: 'Jost', sans-serif;
  height: calc(100vh - 3.75rem);
  margin-top: -3.75rem;
  padding-top: 6rem;
  padding-left: 2rem;
  padding-right: 4rem;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-gutter: stable;
  box-sizing: border-box;
}

/* 텍스트 섹션 (왼쪽 정렬) */
.project-text {
  margin: 0 auto;
  padding-top: 2rem;
  padding-bottom: 2rem;
}

.project-text h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: #8B4513;
  text-transform: uppercase;
  line-height: 1.2;
  margin: 0 0 3rem 0;
  text-align: left;
  max-width: 66.67%;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.project-content {
  text-align: left;
  max-width: 50rem;
}

.project-subtitle {
  font-size: 2rem;
  color: #666;
  margin-bottom: 1rem;
  font-style: italic;
}

.project-date-location {
  font-size: 1rem;
  color: #999;
}

.project-director {
  font-size: 1rem;
  color: #999;
  margin-bottom: 2rem;
}

.project-content p {
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  color: #555;
  font-weight: 400;
}

.more-info {
  margin-top: 2rem;
}

.more-info a {
  color: #8B0000;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.9rem;
  transition: color 0.2s ease;
}

.more-info a:hover {
  color: #A00000;
  text-decoration: underline;
}

/* 이미지 섹션 */
.project-images {
  padding: 4rem 2rem;
  padding-bottom: 6rem; /* 리뷰 섹션과 겹치지 않도록 하단 여백 추가 */
  max-width: 100%; /* 75rem -> 100% (너비 확대) */
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

/* 리뷰 섹션 */
.reviews-section {
  padding: 4rem 2rem;
  max-width: 75rem;
  margin: 0 auto;
  background-color: #f8f8f8;
}

.reviews-section h2 {
  font-size: 1.5rem;
  color: #1E1D1D;
  margin-bottom: 3rem;
  text-align: center;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: 600;
}

.reviews-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
}

.review-item {
  background-color: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 0.125rem 0.625rem rgba(0, 0, 0, 0.1);
}

.review-item blockquote {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #555;
  margin: 0 0 1.5rem 0;
  font-style: italic;
  border-left: 0.1875rem solid #8B0000;
  padding-left: 1rem;
}

.review-item cite {
  font-size: 0.8rem;
  color: #999;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}


/* 모바일 최적화 (768px 이하) */
@media (max-width: 768px) {
  .project-detail {
    height: auto;
    overflow: visible;
    padding-top: 4rem;
    padding-left: 1.5rem;
    padding-right: 1.5rem;
    padding-bottom: 4rem;
  }

  /* 텍스트 섹션 */
  .project-text h1 {
    font-size: 2.5rem;
    max-width: 100%;
    margin-bottom: 2rem;
  }
  
  .project-content {
    max-width: 100%;
  }

  .project-subtitle {
    font-size: 1.5rem;
  }

  /* 이미지 섹션 */
  .project-images {
    padding: 2rem 0; /* 좌우 패딩 제거 (부모 패딩 사용) */
    padding-bottom: 4rem;
  }

  /* 리뷰 섹션 */
  .reviews-section {
    padding: 3rem 1rem;
    padding-bottom: 6rem;
  }

  .reviews-grid {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
}
</style>
