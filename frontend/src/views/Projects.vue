<template>
  <div class="projects">
    <!-- 텍스트 섹션 -->
    <section class="projects-text">
      <h1>PROJECTS</h1>
    </section>

    <!-- 프로젝트 이미지 섹션 -->
    <section class="projects-content">
      <div class="project-item">
        <div class="project-image">
          <img src="../imgs/019_Gregor Schulz, Maximilian Paier und Ensemble_Arturo Ui_©SLT - Tobias Witzgall_LT1_2433 1.png" alt="Arturo Ui Performance">
        </div>
        <div class="project-info">
          <div class="project-title">ARTURO UI</div>
          <div class="project-details">
            <div class="project-date-location">02.22.2025, Salzburg State Theater</div>
            <div class="project-expand" @click="goToProjectDetail">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M7 17L17 7M17 7H7M17 7V17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToProjectDetail = () => {
  router.push('/projects/arturo-ui')
}
</script>

<style scoped>
.projects {
  background-color: white;
  color: #333;
  font-family: 'Arial', sans-serif;
  min-height: calc(100vh - 60px); /* 헤더 높이 제외 */
  margin-top: -60px; /* 헤더 패딩 상쇄 */
  padding: calc(60px + 2rem) 0 2rem 0; /* 헤더 높이 + 여백 */
}

/* 텍스트 섹션 */
.projects-text {
  padding: 2rem 2rem 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  font-size: 4rem;
  font-weight: bold;
  color: #2E86AB;
  margin: 0;
  text-align: left;
  letter-spacing: 3px;
}

/* 프로젝트 콘텐츠 섹션 */
.projects-content {
  padding: 0 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.project-item {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.project-image {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  border-radius: 8px;
  overflow: hidden;
}

.project-image img {
  width: 100%;
  height: auto;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.project-image:hover img {
  transform: scale(1.02);
}

.project-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1rem;
}

.project-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
}

.project-details {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.project-date-location {
  font-size: 1rem;
  color: #666;
  font-weight: 500;
}

.project-expand {
  color: #2E86AB;
  cursor: pointer;
  transition: color 0.3s ease;
}

.project-expand:hover {
  color: #1a5a7a;
}

.project-expand svg {
  transition: transform 0.3s ease;
}

.project-expand:hover svg {
  transform: rotate(45deg);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .projects-text {
    padding: 2rem 1rem 3rem 1rem;
  }
  
  h1 {
    font-size: 2.5rem;
    text-align: center;
  }
  
  .projects-content {
    padding: 0 1rem;
  }
  
  .project-info {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .project-details {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  h1 {
    font-size: 2rem;
  }
  
  .project-title {
    font-size: 1.2rem;
  }
  
  .project-date-location {
    font-size: 0.9rem;
  }
}
</style>
