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

    <!-- 이미지 섹션 (크기 비율대로) -->
    <section class="project-images" v-if="project">
      <div class="image-grid">
        <!-- 첫 번째 행 -->
        <div class="image-row">
          <div class="image-item" v-if="project.mainImageUrl">
            <img :src="project.mainImageUrl" :alt="project.title">
          </div>
          <div class="image-item" v-if="project.horizontal1ImageUrl">
            <img :src="project.horizontal1ImageUrl" :alt="project.title">
          </div>
          <div class="image-item" v-if="project.horizontal2ImageUrl">
            <img :src="project.horizontal2ImageUrl" :alt="project.title">
          </div>
        </div>
        <!-- 두 번째 행 -->
        <div class="image-row">
          <div class="image-item" v-if="project.vertical1ImageUrl">
            <img :src="project.vertical1ImageUrl" :alt="project.title">
          </div>
          <div class="image-item" v-if="project.vertical2ImageUrl">
            <img :src="project.vertical2ImageUrl" :alt="project.title">
          </div>
        </div>
      </div>
    </section>

    <!-- 리뷰 섹션 -->
    <section class="reviews-section" v-if="project">
      <h2>REVIEWS</h2>
      <div class="reviews-grid">
        <div class="review-item" v-if="project.review1Text">
          <blockquote>
            „{{ project.review1Text }}"
          </blockquote>
          <cite>{{ project.review1Source }}</cite>
        </div>
        <div class="review-item" v-if="project.review2Text">
          <blockquote>
            „{{ project.review2Text }}"
          </blockquote>
          <cite>{{ project.review2Source }}</cite>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import axios from '../api/axios'
import { formatDate, toAbsoluteUrl } from '../utils/commonHelpers'
import { calculateRowHeight } from '../utils/imageOptimization'
import { logError } from '../utils/errorHandler'
import mainImg from '../imgs/project_detail/main.jpg'
import horizontal1Img from '../imgs/project_detail/horizontal1.jpg'
import horizontal2Img from '../imgs/project_detail/horizontal2.jpg'
import vertical1Img from '../imgs/project_detail/verticle1.jpg'
import vertical2Img from '../imgs/project_detail/verticle2.jpg'

const route = useRoute()

// 하드코딩된 기본 프로젝트 데이터 (유지)
const hardcodedProject = {
  title: 'THE RESISTIBLE RISE OF ARTURO UI',
  subtitle: 'Collaboration with Salzburger Landestheater',
  premiereDate: '2025-02-22',
  location: 'Salzburg State Theatre',
  director: 'Alexandra Liedtke',
  description1: "We joined the production of Bertolt Brecht's The Resistible Rise of Arturo Ui as composers and performers, creating all of the music for the show using the sound of the violin as the central element.",
  description2: "Throughout 13 performances, we appeared on stage in a minor role while performing live alongside the actors and dancers. Sharing the stage with such a talented ensemble made this collaboration a particularly meaningful experience for us.",
  description3: "Improvisation was an important part of our contribution. Playing close to the actors allowed us to respond to their performances in real time, adding another layer of energy to the scenes.",
  thankYouText: "We would like to thank Alexandra Liedtke and Paul Blackman for their trust and collaboration.",
  moreInfoUrl: 'https://www.salzburger-landestheater.at/en/produktionen/der-aufhaltsame-aufstieg-des-arturo-ui.html?m=537',
  mainImageUrl: mainImg,
  horizontal1ImageUrl: horizontal1Img,
  horizontal2ImageUrl: horizontal2Img,
  vertical1ImageUrl: vertical1Img,
  vertical2ImageUrl: vertical2Img,
  review1Text: "Eine gelungene expressiv körperbetonte Interpretation – die von der anfänglichen Lächerlichkeit des Unbeholfenen, zur erschreckend blutrünstigen Unmenschlichkeit entartet. Eine fantastische Umsetzung eines erschreckend aktuellen Stoffs.",
  review1Source: "Reichenhaller Tagblatt",
  review2Text: "Der Abend enthält starke Szenen, etwa den Rhetorik-Unterricht. Diesen nimmt Ui bei einem Schauspieler, köstlich gespielt von Michael Maertens in Form einer Videoeinblendung. Treffsicher die Persiflage auf politische Theatralik.",
  review2Source: "Salzburger Nachrichten"
}

const project = ref({ ...hardcodedProject })

// 프로젝트 데이터 로드
const loadProject = async () => {
  const urlSlug = route.params.slug
  
  if (!urlSlug) {
    // slug가 없으면 하드코딩된 데이터 사용
    return
  }
  
  try {
    const response = await axios.get(`/projects/slug/${urlSlug}`)
    const projectData = response.data.data || response.data
    
    if (projectData) {
      // API 데이터를 하드코딩 데이터와 병합 (하드코딩 데이터 우선)
      project.value = {
        ...hardcodedProject,
        ...projectData,
        // 이미지 URL 변환
        mainImageUrl: projectData.mainImageUrl ? toAbsoluteUrl(projectData.mainImageUrl) : hardcodedProject.mainImageUrl,
        horizontal1ImageUrl: projectData.horizontal1ImageUrl ? toAbsoluteUrl(projectData.horizontal1ImageUrl) : hardcodedProject.horizontal1ImageUrl,
        horizontal2ImageUrl: projectData.horizontal2ImageUrl ? toAbsoluteUrl(projectData.horizontal2ImageUrl) : hardcodedProject.horizontal2ImageUrl,
        vertical1ImageUrl: projectData.vertical1ImageUrl ? toAbsoluteUrl(projectData.vertical1ImageUrl) : hardcodedProject.vertical1ImageUrl,
        vertical2ImageUrl: projectData.vertical2ImageUrl ? toAbsoluteUrl(projectData.vertical2ImageUrl) : hardcodedProject.vertical2ImageUrl,
        // 필드 매핑
        director: projectData.director || hardcodedProject.director,
        thankYouText: projectData.thankYouText || hardcodedProject.thankYouText
      }
    }
  } catch (error) {
    logError(error, '프로젝트 데이터 로드')
    // 에러 발생 시 하드코딩된 데이터 사용
  }
}

// 이미지 비율에 맞춰 행 높이 조정
onMounted(async () => {
  await loadProject()
  nextTick(() => {
    const rows = document.querySelectorAll('.image-row')
    rows.forEach((row, rowIndex) => {
      const images = row.querySelectorAll('img')
      let loadedCount = 0
      const totalImages = images.length
      
      if (totalImages === 0) return
      
      const checkAndCalculate = () => {
        loadedCount++
        if (loadedCount === totalImages) {
          calculateRowHeight(row, images, rowIndex)
        }
      }
      
      images.forEach((img) => {
        if (img.complete && img.naturalWidth > 0) {
          checkAndCalculate()
        } else {
          img.addEventListener('load', checkAndCalculate, { once: true })
        }
      })
    })
  })
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

/* 이미지 섹션 (크기 비율대로) */
.project-images {
  padding: 4rem 2rem;
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
}

.image-row:nth-child(1) {
  min-height: 18.75rem;
  height: 25rem;
}

.image-row:nth-child(2) {
  min-height: 15.625rem;
  height: 20rem;
}

.image-item {
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.image-row:nth-child(1) .image-item:nth-child(1) {
  flex: 1;
}

.image-row:nth-child(1) .image-item:nth-child(2) {
  flex: 2;
}

.image-row:nth-child(1) .image-item:nth-child(3) {
  flex: 1;
}

.image-row:nth-child(2) .image-item {
  flex: 1;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
