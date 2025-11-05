<template>
  <div class="project-detail">
    <!-- 텍스트 섹션 (왼쪽 정렬) -->
    <section class="project-text" v-if="project">
      <h1>{{ project.title }}</h1>
      <div class="project-content">
        <div class="project-subtitle">{{ project.subtitle }}</div>
        <div class="project-date-location">Premiere: {{ formatDate(project.premiereDate, 'date', 'en-US') }} · {{ project.location }}</div>
        <div class="project-director" v-if="project.director">Director: {{ project.director }}</div>
        <p v-if="project.description1">{{ project.description1 }}</p>
        <p v-if="project.description2">{{ project.description2 }}</p>
        <p v-if="project.description3">{{ project.description3 }}</p>
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
      <div class="image-grid">
        <div class="image-row">
          <div class="image-item" v-for="(imageUrl, index) in projectImageUrls" :key="index">
            <img :src="imageUrl" :alt="`${project.title} image ${index + 1}`" @load="onImageLoad" />
          </div>
        </div>
      </div>
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import axios from '../api/axios'
import { formatDate, toAbsoluteUrl } from '../utils/commonHelpers'
import { calculateRowHeight } from '../utils/imageOptimization'
import { logError } from '../utils/errorHandler'

const route = useRoute()

const project = ref(null)

// 프로젝트 이미지 URL 배열 생성
const projectImageUrls = computed(() => {
  if (!project.value) return []
  
  const urls = []
  
  // 기존 이미지 필드들 (하위 호환성)
  if (project.value.mainImageUrl) urls.push(project.value.mainImageUrl)
  if (project.value.horizontal1ImageUrl) urls.push(project.value.horizontal1ImageUrl)
  if (project.value.horizontal2ImageUrl) urls.push(project.value.horizontal2ImageUrl)
  if (project.value.vertical1ImageUrl) urls.push(project.value.vertical1ImageUrl)
  if (project.value.vertical2ImageUrl) urls.push(project.value.vertical2ImageUrl)
  
  // 새로운 imageUrls 배열 (우선순위 높음)
  if (project.value.imageUrls && Array.isArray(project.value.imageUrls) && project.value.imageUrls.length > 0) {
    return project.value.imageUrls.map(url => toAbsoluteUrl(url))
  }
  
  return urls
})

// 프로젝트 리뷰 배열 생성
const projectReviews = computed(() => {
  if (!project.value) return []
  
  // 새로운 reviews 배열 사용 (우선순위 높음)
  if (project.value.reviews && Array.isArray(project.value.reviews) && project.value.reviews.length > 0) {
    return project.value.reviews.filter(review => review.text && review.text.trim() !== '')
  }
  
  // 하위 호환성: 기존 review1, review2를 배열로 변환
  const reviews = []
  if (project.value.review1Text && project.value.review1Text.trim() !== '') {
    reviews.push({ text: project.value.review1Text, source: project.value.review1Source || '' })
  }
  if (project.value.review2Text && project.value.review2Text.trim() !== '') {
    reviews.push({ text: project.value.review2Text, source: project.value.review2Source || '' })
  }
  
  return reviews
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
        mainImageUrl: projectData.mainImageUrl ? toAbsoluteUrl(projectData.mainImageUrl) : null,
        imageUrls: projectData.imageUrls ? projectData.imageUrls.map(url => toAbsoluteUrl(url)) : []
      }
    }
  } catch (error) {
    logError(error, '프로젝트 데이터 로드')
    project.value = null
  }
}

// 이미지 로드 시 행 높이 계산
const onImageLoad = (event) => {
  nextTick(() => {
    const img = event.target
    const row = img.closest('.image-row')
    if (!row) return
    
    const images = row.querySelectorAll('img')
    // 모든 이미지가 로드되었는지 확인
    const allLoaded = Array.from(images).every(img => img.complete && img.naturalWidth > 0)
    if (allLoaded) {
      calculateRowHeight(row, images, 0)
    }
  })
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
  max-width: 75rem;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.image-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 62.5rem;
  width: 100%;
}

.image-row {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  min-height: 18.75rem;
  align-items: flex-start;
}

.image-item {
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  height: 100%;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 비율 유지하면서 축소 */
  object-position: center;
  transition: transform 0.3s ease;
  display: block;
}

.image-item:hover img {
  transform: scale(1.05);
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

</style>
