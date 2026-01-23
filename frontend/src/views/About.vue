<template>
  <div class="about">
    <!-- 왼쪽: 타이틀 영역 -->
    <div class="about-title-section">
      <h1>ABOUT</h1>
    </div>

    <!-- 오른쪽: 콘텐츠 영역 -->
    <div class="about-content-section">
      <!-- 텍스트 섹션 -->
      <div class="about-content">
        <p>
          TWIIIINS IS A VIOLIN DUO FROM SOUTH KOREA AND GERMANY,<br>
          KNOWN FOR THEIR ENERGETIC AND GENRE-BLENDING PERFORMANCES.<br>
          THEY COMBINE CLASSICAL MUSIC WITH POP, FUNK, FILM SCORES, JAZZ, AND ROCK,<br>
          CREATING A UNIQUE AND DYNAMIC SOUND.<br>
        </p>
        <p>
          WITH THEIR LIVELY STAGE PRESENCE AND PASSION FOR LIVE MUSIC,<br>
          TWIIIINS CAPTIVATES AUDIENCES, TURNING EACH PERFORMANCE INTO AN ENGAGING EXPERIENCE.<br>
          MORE THAN JUST A DUO, THEY AIM TO SPREAD JOY THROUGH THEIR MUSIC,<br>
          OFFERING A FRESH TAKE ON VIOLIN PERFORMANCES<br>
          THAT CONNECT WITH PEOPLE ACROSS DIFFERENT GENRES AND STYLES.
        </p>
      </div>

      <!-- 이미지 그리드 섹션 -->
      <div class="about-images">
        <div class="image-grid">
          <!-- 첫 번째 행 -->
          <div class="image-row">
            <div class="image-item">
              <img src="../imgs/About/20250719-P1250713 1.png" alt="TWIIIINS Performance">
            </div>
            <div class="image-item">
              <img src="../imgs/About/20250719-P1250816 1.png" alt="TWIIIINS Performance">
            </div>
            <div class="image-item">
              <img src="../imgs/About/20250719-P1250891 1.png" alt="TWIIIINS Performance">
            </div>
          </div>
          <!-- 두 번째 행 -->
          <div class="image-row">
            <div class="image-item">
              <img src="../imgs/About/unnamed 1.png" alt="TWIIIINS Performance">
            </div>
            <div class="image-item">
              <img src="../imgs/About/TwinsMusicApril2024-9 1.png" alt="TWIIIINS Performance">
            </div>
            <div class="image-item">
              <img src="../imgs/About/TwinsMusicApril2024-6 1.png" alt="TWIIIINS Performance">
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about {
  background-color: white;
  color: #1E1D1D;
  font-family: 'Jost', sans-serif;
  height: calc(100vh - 3.75rem);
  overflow-y: auto;
  overflow-x: hidden;
  padding-top: 6rem;
  padding-left: 2rem;
  padding-right: 2rem;
  scrollbar-gutter: stable;
  box-sizing: border-box;
  display: flex;
  gap: 4rem;
  align-items: flex-start;
}

/* 왼쪽: 타이틀 영역 */
.about-title-section {
  flex-shrink: 0;
  width: auto;
  padding-top: 2rem;
}

.about-title-section h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: #8B4513;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
  text-align: left;
}

/* 오른쪽: 콘텐츠 영역 */
.about-content-section {
  flex: 1;
  min-width: 0;
  padding-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 4rem;
}

/* 텍스트 섹션 */
.about-content {
  text-align: left;
  max-width: 50rem;
}

.about-content p {
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  color: #555;
  font-weight: 400;
  white-space: pre-line;
}

/* 이미지 그리드 섹션 */
.about-images {
  display: flex;
  justify-content: flex-start;
  width: 100%;
  padding-bottom: 6rem;
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
  gap: 0.1rem;
  height: 18.75rem;
}

.image-row:nth-child(2) {
  height: 15.625rem;
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

.image-row:nth-child(2) .image-item:nth-child(1) {
  flex: 1;
}

.image-row:nth-child(2) .image-item:nth-child(2) {
  flex: 2;
}

.image-row:nth-child(2) .image-item:nth-child(3) {
  flex: 1;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
  display: block;
}

.image-item:hover img {
  transform: scale(1.05);
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .about {
    flex-direction: column;
    gap: 2rem;
    padding-left: 1rem; /* 모바일 여백 조정 */
    padding-right: 1rem;
    height: auto; /* 모바일에서는 자연스러운 스크롤 허용 */
    overflow-y: visible;
    padding-bottom: 4rem; /* 하단 여백 추가 */
  }

  .about-title-section {
    padding-top: 1rem;
  }

  .about-content-section {
    padding-top: 0;
    gap: 2rem;
  }

  .about-content {
    max-width: 100%;
  }

  .about-content p {
    font-size: 0.85rem; /* 폰트 크기 축소 */
    line-height: 1.5;
  }

  /* 모바일에서 이미지 그리드: 비대칭 2열 레이아웃 (10컬럼 그리드) */
  .image-grid {
    display: grid;
    grid-template-columns: repeat(10, 1fr);
    gap: 0.1rem;
  }

  .image-row {
    display: contents;
  }
  
  .image-item {
    width: 100%;
  }

  /* 1번 이미지: 좁게 (4/10) */
  .image-row:nth-child(1) .image-item:nth-child(1) {
    grid-column: span 4;
    height: 60vw;
  }
  
  /* 2번 이미지: 넓게 (6/10) */
  .image-row:nth-child(1) .image-item:nth-child(2) {
    grid-column: span 6;
    height: 60vw;
  }

  /* 3번 이미지: 넓게 (6/10) - 두 번째 줄 시작 */
  .image-row:nth-child(1) .image-item:nth-child(3) {
    grid-column: span 5;
    height: 50vw;
  }

  /* 4번 이미지: 좁게 (4/10) */
  .image-row:nth-child(2) .image-item:nth-child(1) {
    grid-column: span 5;
    height: 50vw;
  }

  /* 5번 이미지: 반반 (5/10) - 세 번째 줄 시작 */
  .image-row:nth-child(2) .image-item:nth-child(2) {
    grid-column: span 5;
    height: 45vw;
  }

  /* 6번 이미지: 반반 (5/10) */
  .image-row:nth-child(2) .image-item:nth-child(3) {
    grid-column: span 5;
    height: 45vw;
  }
}
</style>

