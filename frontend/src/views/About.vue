<template>
  <div class="about">
    <!-- 텍스트 섹션 -->
    <section class="about-text">
      <h1>ABOUT</h1>
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
    </section>

    <!-- 이미지 그리드 섹션 -->
    <section class="about-images">
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
            <img src="../imgs/About/TwinsMusicApril2024-6 1.png" alt="TWIIIINS Performance">
          </div>
          <div class="image-item">
            <img src="../imgs/About/TwinsMusicApril2024-9 1.png" alt="TWIIIINS Performance">
          </div>
          <div class="image-item">
            <img src="../imgs/About/unnamed 1.png" alt="TWIIIINS Performance">
          </div>
        </div>
      </div>
    </section>
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
  scrollbar-gutter: stable;
  box-sizing: border-box;
}

/* 텍스트 섹션 */
.about-text {
  margin: 0 auto;
  padding-top: 2rem;
}

.about-text h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: #8B4513;
  text-transform: uppercase;
  line-height: 1;
  margin: 0 0 3rem 0;
  text-align: left;
}

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
</style>

