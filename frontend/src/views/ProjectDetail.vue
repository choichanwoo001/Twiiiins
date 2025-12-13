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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
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

// 이미지 로드 시 행 높이 계산
const onImageLoad = (event) => {
  nextTick(() => {
    const img = event.target
    const row = img.closest('.image-row')
    if (!row) return
    
    // 이미지 로드 직후 즉시 크기 제한 (calculateRowHeight 실행 전)
    const imageItem = img.closest('.image-item')
    if (imageItem) {
      // 임시로 행 너비에 맞게 제한
      const rowWidth = row.offsetWidth || 1000 // 기본값
      const maxItemWidth = rowWidth - 8 // gap 고려
      if (img.naturalWidth && img.naturalHeight) {
        const aspectRatio = img.naturalWidth / img.naturalHeight
        const maxHeight = 300 // calculateRowHeight의 maxHeight와 동일
        const calculatedWidth = maxHeight * aspectRatio
        // 행 너비를 초과하지 않도록 제한
        imageItem.style.width = `${Math.min(calculatedWidth, maxItemWidth)}px`
        imageItem.style.height = `${Math.min(maxHeight, maxItemWidth / aspectRatio)}px`
      }
    }
    
    const images = row.querySelectorAll('img')
    // 모든 이미지가 로드되었는지 확인
    const allLoaded = Array.from(images).every(img => img.complete && img.naturalWidth > 0)
    if (allLoaded) {
      // 모든 이미지가 로드되면 정확한 계산 수행
      calculateRowHeight(row, images, 0)
    } else {
      // 일부 이미지만 로드된 경우에도 계산 (점진적 로딩 대응)
      const loadedImages = Array.from(images).filter(img => img.complete && img.naturalWidth > 0)
      if (loadedImages.length > 0) {
        calculateRowHeight(row, loadedImages, 0)
      }
    }
  })
}

// 이미지 URL이 변경될 때도 재계산
const recalculateImages = () => {
  nextTick(() => {
    const rows = document.querySelectorAll('.project-images .image-row')
    rows.forEach(row => {
      const images = row.querySelectorAll('img')
      const loadedImages = Array.from(images).filter(img => img.complete && img.naturalWidth > 0)
      if (loadedImages.length > 0) {
        calculateRowHeight(row, loadedImages, 0)
      }
    })
  })
}

onMounted(async () => {
  await loadProject()
  // 이미지가 로드된 후 재계산
  setTimeout(() => {
    recalculateImages()
  }, 500)
})

// 프로젝트 이미지 URL이 변경될 때 재계산
watch(() => projectImageUrls.value, () => {
  recalculateImages()
}, { deep: true })

</script>

<style scoped>
.project-detail {
  background-color: white;
  color: #1E1D1D;
  font-family: 'Josefin Sans', sans-serif;
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
  width: 100%;
  max-width: 100%;
  /* 행 내 이미지가 행 너비를 초과하지 않도록 */
  box-sizing: border-box;
}

.image-item {
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  height: 100%;
  /* 초기 최대 너비 제한 - 행 너비를 절대 초과하지 않도록 */
  max-width: 25rem;
  /* calculateRowHeight에서 너비가 설정될 때까지 임시로 작은 크기 */
  width: auto;
  /* 이미지가 컨테이너를 넘어가지 않도록 */
  box-sizing: border-box;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 비율 유지하면서 축소 */
  object-position: center;
  transition: transform 0.3s ease;
  display: block;
  /* 이미지 자체가 컨테이너를 절대 초과하지 않도록 */
  max-width: 100%;
  max-height: 100%;
  /* 원본 크기로 표시되지 않도록 보장 */
  box-sizing: border-box;
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
