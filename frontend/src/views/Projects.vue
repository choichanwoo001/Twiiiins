<template>
  <div class="projects">
    <!-- 좌측 고정 타이틀 -->
    <aside class="projects-title">
      <h1>PROJECTS</h1>
    </aside>

    <!-- 우측 콘텐츠 -->
    <main class="projects-content">
      <!-- 이미지 -->
      <figure class="project-figure">
        <img
          src="../imgs/project_cover.jpg"
          alt="Project hero"
        />
      </figure>

      <!-- 캡션 행 -->
      <div class="project-caption">
        <div class="caption-left">ARTURO UI</div>
        <div class="caption-right">
          <span>02.22.2025, Salzburg State Theater</span>
          <svg class="caption-arrow" width="18" height="18" viewBox="0 0 24 24" fill="none" @click="goToProjectDetail">
            <path d="M7 17L17 7M17 7H9M17 7V15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>  
    </main>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'Projects',
  setup() {
    const router = useRouter()
    
    const goToProjectDetail = () => {
      router.push('/projects/arturo-ui')
    }
    
    return {
      goToProjectDetail
    }
  }
}
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
  font-weight: 500;
  letter-spacing: 0.12em;
  color:#6f93a0;               /* 이미지처럼 청록빛 톤 */
  text-transform: uppercase;
  line-height:1;
  margin:0;
}

/* 우측 콘텐츠 */
.projects-content{
  max-width: 800px; /* 이미지 크기 줄임 */
  width: 100%;
  margin-right: 2rem;
  justify-self: end;           /* 우측 정렬 느낌 */
  padding-top: 2rem; /* 콘텐츠를 아래로 이동 */
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
.project-figure img{
  width:100%;
  height:100%;
  object-fit: cover;
  display:block;
  transform: translateZ(0);
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
}
.caption-arrow{
  flex:0 0 auto;
  color:#666;
  cursor: pointer;
  transition: color 0.2s ease;
}

.caption-arrow:hover {
  color: #333;
}

/* 호버 미세 인터랙션(선택) */
.project-figure:hover img{
  transform: scale(1.01);
  transition: transform .25s ease;
}

</style>
