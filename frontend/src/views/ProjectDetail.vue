<template>
  <div class="project-detail">
    <!-- 프로젝트 제목 섹션 -->
    <section class="project-header">
      <div class="project-label">PROJECT</div>
      <div class="project-title-section">
        <h1>Arturo Ui</h1>
        <div class="project-date-location">02.22.2025, Salzburg State Theater</div>
      </div>
    </section>

    <!-- 메인 이미지 -->
    <section class="main-image">
      <img src="../imgs/019_Gregor Schulz, Maximilian Paier und Ensemble_Arturo Ui_©SLT - Tobias Witzgall_LT1_2433 1.png" alt="Arturo Ui Performance">
    </section>

    <!-- 첫 번째 텍스트 블록 -->
    <section class="text-block">
      <p>
        BRECHT SUPERBLY DRAWS A GANGSTERS' MILIEU THAT SERVES AS A BLEAK MIRROR OF POLITICAL REALITIES. THE STOCK MARKET CRASH HAS HIT CHICAGO HARD. PEOPLE HAVE LESS MONEY TO SPEND AND ONE OF THE FIRST THINGS THEY STOP BUYING IS VEGETABLES!
      </p>
    </section>

    <!-- 두 번째 이미지 블록 -->
    <section class="image-block">
      <div class="image-row">
        <div class="image-item">
          <img src="../imgs/20250719-P1250713 1.png" alt="Arturo Ui Performance">
        </div>
      </div>
      <div class="image-row">
        <div class="image-item">
          <img src="../imgs/20250719-P1250816 1.png" alt="Arturo Ui Performance">
        </div>
      </div>
    </section>

    <!-- 두 번째 텍스트 블록 -->
    <section class="text-block">
      <p>
        A certain Arturo Ui sees an opportunity to make a name for himself. He offers to whet the population's appetite for cauliflower by threatening violence. Of course, the mob rejects the offer unanimously. They are hoping that the respected politician Dogsborough will help them get a loan from the city council. When Dogsborough declines, he becomes the target of a dark conspiracy.
      </p>
    </section>

    <!-- 세 번째 이미지 블록 (그리드 레이아웃) -->
    <section class="image-grid">
      <div class="grid-left">
        <div class="image-item">
          <img src="../imgs/20250719-P1250891 1.png" alt="Arturo Ui Performance">
        </div>
        <div class="image-item">
          <img src="../imgs/TwinsMusicApril2024-6 1.png" alt="Arturo Ui Performance">
        </div>
      </div>
      <div class="grid-right">
        <div class="image-item">
          <img src="../imgs/TwinsMusicApril2024-9 1.png" alt="Arturo Ui Performance">
        </div>
        <div class="german-text">
          Der aufhaltsame Aufstieg eines Mannes zur Macht
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.project-detail {
  background-color: white;
  color: #333;
  font-family: 'Arial', sans-serif;
  min-height: calc(100vh - 60px); /* 헤더 높이 제외 */
  margin-top: -60px; /* 헤더 패딩 상쇄 */
  padding-top: 60px; /* 헤더 높이만큼 상단 패딩 */
}

/* 프로젝트 헤더 */
.project-header {
  padding: 2rem 2rem 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.project-label {
  font-size: 0.9rem;
  color: #999;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.project-title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.project-title-section h1 {
  font-size: 4rem;
  font-weight: bold;
  color: #333;
  margin: 0;
  line-height: 1;
}

.project-date-location {
  font-size: 1rem;
  color: #999;
  text-align: right;
}

/* 메인 이미지 */
.main-image {
  padding: 0 2rem 3rem 2rem;
  max-width: 1200px;
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
  max-width: 1200px;
  margin: 0 auto;
}

.text-block p {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #555;
  margin: 0;
  max-width: 600px;
  margin-left: auto;
  text-align: right;
}

/* 이미지 블록 */
.image-block {
  padding: 0 2rem 3rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.image-row {
  margin-bottom: 1rem;
}

.image-row:last-child {
  margin-bottom: 0;
}

.image-item {
  width: 100%;
}

.image-item img {
  width: 100%;
  height: auto;
  object-fit: cover;
}

/* 이미지 그리드 */
.image-grid {
  padding: 0 2rem 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.grid-left {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.grid-right {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.german-text {
  font-size: 1.2rem;
  color: #8B0000;
  font-weight: bold;
  text-align: center;
  padding: 2rem 0;
  line-height: 1.4;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .project-header {
    padding: 2rem 1rem 3rem 1rem;
  }
  
  .project-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .project-title-section h1 {
    font-size: 2.5rem;
  }
  
  .project-date-location {
    text-align: left;
  }
  
  .main-image,
  .text-block,
  .image-block,
  .image-grid {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .text-block p {
    text-align: left;
    margin-left: 0;
  }
  
  .image-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .german-text {
    font-size: 1rem;
    padding: 1rem 0;
  }
}

@media (max-width: 480px) {
  .project-title-section h1 {
    font-size: 2rem;
  }
  
  .text-block p {
    font-size: 0.8rem;
  }
  
  .german-text {
    font-size: 0.9rem;
  }
}
</style>
