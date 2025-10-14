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
          src="../imgs/019_Gregor Schulz, Maximilian Paier und Ensemble_Arturo Ui_©SLT - Tobias Witzgall_LT1_2433 1.png"
          alt="Project hero"
        />
      </figure>

      <!-- 캡션 행 -->
      <div class="project-caption">
        <div class="caption-left">ARTURO UI</div>
        <div class="caption-right">
          <span>02.22.2025, Salzburg State Theater</span>
          <svg class="caption-arrow" width="18" height="18" viewBox="0 0 24 24" fill="none">
            <path d="M7 17L17 7M17 7H9M17 7V15" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 레이아웃 */
.projects {
  background:#fff;
  color:#222;
  min-height: calc(100vh - 60px);
  margin-top:-60px;            /* 상단 고정 헤더 보정 사용 중이라면 유지 */
  padding-top: calc(60px + 4rem); /* 헤더 높이 + 충분한 여백 */
  display:grid;
  grid-template-columns: 1fr 2.2fr; /* 좌(타이틀) : 우(콘텐츠) 비율 */
  align-items: start;
  gap: 4rem; /* 제목과 이미지 사이 공백 증가 */
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  padding-left: 2rem;
  padding-right: 2rem;
}

/* 좌측 타이틀 */
.projects-title {
  display:flex;
  align-items:flex-start;
  justify-content:flex-start;
  padding-left: 1rem;
  padding-top: 2rem; /* 제목을 아래로 이동 */
}
.projects-title h1{
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight:700;
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
}

/* 호버 미세 인터랙션(선택) */
.project-figure:hover img{
  transform: scale(1.01);
  transition: transform .25s ease;
}

/* 반응형 */
@media (max-width: 1024px){
  .projects{
    grid-template-columns: 0.9fr 2fr;
    gap: 3rem; /* 태블릿에서도 공백 유지 */
    padding-left: 1rem;
    padding-right: 1rem;
  }
}
@media (max-width: 768px){
  .projects{
    grid-template-columns: 1fr;
    gap: 1rem;
    padding-left: 1rem;
    padding-right: 1rem;
  }
  .projects-title{
    justify-content: center;
    padding-left: 0;
    padding-top: 1rem;
  }
  .projects-title h1{
    letter-spacing: 0.08em;
  }
  .projects-content{
    margin: 0 auto;
    justify-self: center;
    padding: 0 1rem;
    padding-top: 1rem;
  }
  .project-caption{
    padding: 0;
  }
}
</style>
