<template>
  <div class="project-detail">
    <!-- 프로젝트 제목 섹션 -->
    <section class="project-header" v-if="project">
      <div class="project-title-section">
        <h1>{{ project.title }}</h1>
        <div class="project-subtitle">{{ project.subtitle }}</div>
        <div class="project-date-location">Premiere: {{ formatDate(project.premiereDate) }} · {{ project.location }}</div>
      </div>
    </section>

    <!-- 메인 이미지 -->
    <section class="main-image" v-if="project">
      <img :src="project.mainImageUrl" :alt="project.title">
    </section>

    <!-- 첫 번째 텍스트 블록 -->
    <section class="text-block" v-if="project">
      <p>{{ project.description1 }}</p>
    </section>

    <!-- 두 번째 이미지 블록 (좌우 분할) -->
    <section class="image-split" v-if="project">
      <div class="split-left">
        <div class="image-item">
          <img :src="project.horizontal1ImageUrl" :alt="project.title">
        </div>
      </div>
      <div class="split-right">
        <div class="image-item">
          <img :src="project.horizontal2ImageUrl" :alt="project.title">
        </div>
      </div>
    </section>

    <!-- 두 번째 텍스트 블록 -->
    <section class="text-block" v-if="project">
      <p>{{ project.description2 }}</p>
    </section>

    <!-- 세 번째 이미지 블록 (3분할 그리드) -->
    <section class="image-trio" v-if="project">
      <div class="trio-left">
        <div class="image-item">
          <img :src="project.vertical1ImageUrl" :alt="project.title">
        </div>
      </div>
      <div class="trio-center">
        <div class="text-content" v-if="project">
          <p v-if="project.description3">{{ project.description3 }}</p>
          <p v-if="project.thankYouText">{{ project.thankYouText }}</p>
          <div class="more-info" v-if="project.moreInfoUrl">
            <a :href="project.moreInfoUrl" target="_blank">
              More Information →
            </a>
          </div>
        </div>
      </div>
      <div class="trio-right">
        <div class="image-item">
          <img :src="project.vertical2ImageUrl" :alt="project.title">
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
import { ref } from 'vue'
import mainImg from '../imgs/project_detail/main.jpg'
import horizontal1Img from '../imgs/project_detail/horizontal1.jpg'
import horizontal2Img from '../imgs/project_detail/horizontal2.jpg'
import vertical1Img from '../imgs/project_detail/verticle1.jpg'
import vertical2Img from '../imgs/project_detail/verticle2.jpg'

const project = ref({
  title: 'THE RESISTIBLE RISE OF ARTURO UI',
  subtitle: 'Collaboration with Salzburger Landestheater',
  premiereDate: '2025-02-22',
  location: 'Salzburg State Theatre',
  description1: "We joined the production of Bertolt Brecht's The Resistible Rise of Arturo Ui as composers and performers, creating all of the music for the show using the sound of the violin as the central element.",
  description2: "The production explores themes of power, manipulation, and the rise of authoritarianism through the story of Arturo Ui, a small-time gangster who rises to power in 1930s Chicago.",
  description3: "Our musical contribution to this production involved creating an original score that blends classical violin techniques with contemporary sound design, enhancing the dramatic impact of Brecht's political allegory.",
  thankYouText: "We would like to thank Alexandra Liedtke and Paul Blackman for their trust and collaboration.",
  mainImageUrl: mainImg,
  horizontal1ImageUrl: horizontal1Img,
  horizontal2ImageUrl: horizontal2Img,
  vertical1ImageUrl: vertical1Img,
  vertical2ImageUrl: vertical2Img,
  review1Text: "Eine gelungene expressiv körperbetonte Interpretation – die von der anfänglichen Lächerlichkeit des Unbeholfenen, zur erschreckend blutrünstigen Unmenschlichkeit entartet. Eine fantastische Umsetzung eines erschreckend aktuellen Stoffs.",
  review1Source: "Reichenhaller Tagblatt",
  review2Text: "Der Abend enthält starke Szenen, etwa den Rhetorik-Unterricht. Diesen nimmt Ui bei einem Schauspieler, köstlich gespielt von Michael Maertens in Form einer Videoeinblendung. Treffsicher die Persiflage auf politische Theatralik.",
  review2Source: "Salzburger Nachrichten"
})

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.project-detail {
  background-color: white;
  color: #333;
  font-family: 'Arial', sans-serif;
  min-height: calc(100vh - 3.75rem); /* 헤더 높이 제외 */
  margin-top: -3.75rem; /* 헤더 패딩 상쇄 */
  padding-top: 3.75rem; /* 헤더 높이만큼 상단 패딩 */
}

/* 프로젝트 헤더 */
.project-header {
  padding: 6rem 2rem 1rem 2rem;
  max-width: 75rem;
  margin: 0 auto;
}

.project-title-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.project-title-section h1 {
  font-size: 3.5rem;
  font-weight: bold;
  color: #333;
  margin: 0 0 0.5rem 0;
  line-height: 1.1;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.project-subtitle {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 1rem;
  font-style: italic;
  align-self: flex-start;
}

.project-date-location {
  font-size: 1rem;
  color: #999;
  align-self: flex-end;
  margin-top: 1rem;
}

/* 메인 이미지 */
.main-image {
  padding: 0 2rem 2rem 2rem;
  max-width: 75rem;
  margin: 0 auto;
}

.main-image img {
  width: 100%;
  height: auto;
  object-fit: cover;
}

/* 텍스트 블록 */
.text-block {
  padding: 0 2rem 3rem 2rem;
  max-width: 75rem;
  margin: 0 auto;
}

.text-block p {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #555;
  margin: 0;
  max-width: 37.5rem;
  margin-left: auto;
  text-align: right;
}

/* 이미지 분할 블록 */
.image-split {
  padding: 0 2rem 3rem 2rem;
  max-width: 75rem;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.split-left,
.split-right {
  width: 100%;
}

.image-item {
  width: 100%;
  aspect-ratio: 4/3;
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-item:hover img {
  transform: scale(1.05);
}

/* 이미지 트리오 블록 */
.image-trio {
  padding: 0 2rem 4rem 2rem;
  max-width: 75rem;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1.5fr 1fr;
  gap: 2rem;
  align-items: center;
}

.trio-left,
.trio-right {
  width: 100%;
}

.trio-center {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 2rem 0;
}

.trio-left .image-item,
.trio-right .image-item {
  aspect-ratio: 3/4;
  overflow: hidden;
}

.text-content {
  padding: 2rem 0;
}

.text-content p {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #555;
  margin: 0 0 1.5rem 0;
  max-width: 31.25rem;
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

/* 리뷰 섹션 */
.reviews-section {
  padding: 4rem 2rem;
  max-width: 75rem;
  margin: 0 auto;
  background-color: #f8f8f8;
}

.reviews-section h2 {
  font-size: 1.5rem;
  color: #333;
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
