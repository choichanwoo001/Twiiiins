<template>
  <div class="about">
    <!-- 텍스트 섹션 -->
    <section class="about-text">
      <h1>ABOUT</h1>
      <div class="about-content">
        <p>
          TWIIIINS IS A VIOLIN DUO FROM SOUTH KOREA AND GERMANY, KNOWN FOR THEIR ENERGETIC AND GENRE-BLENDING PERFORMANCES. THEY COMBINE CLASSICAL MUSIC WITH POP, FUNK, FILM SCORES, JAZZ, AND ROCK, CREATING A UNIQUE AND DYNAMIC SOUND.
        </p>
        <p>
          WITH THEIR LIVELY STAGE PRESENCE AND PASSION FOR LIVE MUSIC, TWIIIINS CAPTIVATES AUDIENCES, TURNING EACH PERFORMANCE INTO AN ENGAGING EXPERIENCE. MORE THAN JUST A DUO, THEY AIM TO SPREAD JOY THROUGH THEIR MUSIC, OFFERING A FRESH TAKE ON VIOLIN PERFORMANCES THAT CONNECT WITH PEOPLE ACROSS DIFFERENT GENRES AND STYLES.
        </p>
      </div>
    </section>

    <!-- 이미지 그리드 섹션 -->
    <section class="about-images">
      <div class="image-grid">
        <!-- 첫 번째 행 -->
        <div class="image-row">
          <div class="image-item">
            <img src="../imgs/20250719-P1250713 1.png" alt="TWIIIINS Performance">
          </div>
          <div class="image-item">
            <img src="../imgs/20250719-P1250816 1.png" alt="TWIIIINS Performance">
          </div>
          <div class="image-item">
            <img src="../imgs/20250719-P1250891 1.png" alt="TWIIIINS Performance">
          </div>
        </div>
        <!-- 두 번째 행 -->
        <div class="image-row">
          <div class="image-item">
            <img src="../imgs/TwinsMusicApril2024-6 1.png" alt="TWIIIINS Performance">
          </div>
          <div class="image-item">
            <img src="../imgs/TwinsMusicApril2024-9 1.png" alt="TWIIIINS Performance">
          </div>
          <div class="image-item">
            <img src="../imgs/unnamed 1.png" alt="TWIIIINS Performance">
          </div>
        </div>
      </div>
    </section>

    <!-- 스크롤 인디케이터 -->
    <div class="scroll-indicator">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
  </div>
</template>

<style scoped>
.about {
  background-color: white;
  color: #333;
  font-family: 'Arial', sans-serif;
  min-height: calc(100vh - 60px); /* 헤더 높이 제외 */
  margin-top: -60px; /* 헤더 패딩 상쇄 */
  padding-top: 60px; /* 헤더 높이만큼 상단 패딩 */
}

/* 텍스트 섹션 */
.about-text {
  padding: 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  font-size: 3rem;
  font-weight: bold;
  color: #8B0000;
  margin-bottom: 3rem;
  text-align: left;
  letter-spacing: 2px;
  margin-left: 2rem;
}

.about-content {
  text-align: left;
  max-width: 800px;
  margin-left: 2rem;
}

.about-content p {
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  color: #555;
  font-weight: 400;
}

/* 이미지 그리드 섹션 */
.about-images {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.image-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 1000px;
  margin: 0 auto;
}

.image-row {
  display: flex;
  gap: 0.5rem;
  height: 300px;
}

.image-row:nth-child(2) {
  height: 250px;
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

/* 스크롤 인디케이터 */
.scroll-indicator {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
  color: #666;
}

.scroll-indicator svg {
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .about-text {
    padding: 2rem 1rem;
  }
  
  h1 {
    font-size: 2rem;
    text-align: center;
  }
  
  .about-content {
    text-align: center;
  }
  
  .image-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
  }
  
  .about-images {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .image-grid {
    grid-template-columns: 1fr;
  }
}
</style>

