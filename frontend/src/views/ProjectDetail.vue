<template>
  <div class="project-detail">
    <!-- 프로젝트 제목 섹션 -->
    <section class="project-header">
      <div class="project-title-section">
        <h1>THE RESISTIBLE RISE OF ARTURO UI</h1>
        <div class="project-subtitle">Collaboration with Salzburger Landestheater</div>
        <div class="project-date-location">Premiere: February 22, 2025 · Salzburg State Theatre</div>
      </div>
    </section>

    <!-- 메인 이미지 -->
    <section class="main-image">
      <img src="../imgs/project_detail/main.jpg" alt="Arturo Ui Performance">
    </section>

    <!-- 첫 번째 텍스트 블록 -->
    <section class="text-block">
      <p>
        We joined the production of Bertolt Brecht's The Resistible Rise of Arturo Ui as composers and performers, creating all of the music for the show using the sound of the violin as the central element.
      </p>
    </section>

    <!-- 두 번째 이미지 블록 (좌우 분할) -->
    <section class="image-split">
      <div class="split-left">
        <div class="image-item">
          <img src="../imgs/project_detail/horizontal1.jpg" alt="Arturo Ui Performance">
        </div>
      </div>
      <div class="split-right">
        <div class="image-item">
          <img src="../imgs/project_detail/horizontal2.jpg" alt="Arturo Ui Performance">
        </div>
      </div>
    </section>

    <!-- 두 번째 텍스트 블록 -->
    <section class="text-block">
      <p>
        Throughout 13 performances, we appeared on stage in a minor role while performing live alongside the actors and dancers. Sharing the stage with such a talented ensemble made this collaboration a particularly meaningful experience for us.
      </p>
    </section>

    <!-- 세 번째 이미지 블록 (3분할 그리드) -->
    <section class="image-trio">
      <div class="trio-left">
        <div class="image-item">
          <img src="../imgs/project_detail/verticle1.jpg" alt="Arturo Ui Performance">
        </div>
      </div>
      <div class="trio-center">
        <div class="text-content">
          <p>
            Improvisation was an important part of our contribution. Playing close to the actors allowed us to respond to their performances in real time, adding another layer of energy to the scenes.
          </p>
          <p>
            We would like to thank Alexandra Liedtke and Paul Blackman for their trust and collaboration.
          </p>
          <div class="more-info">
            <a href="https://www.salzburger-landestheater.at/en/produktionen/der-aufhaltsameaufstieg-des-arturo-ui.html?m=537" target="_blank">
              More Information →
            </a>
          </div>
        </div>
      </div>
      <div class="trio-right">
        <div class="image-item">
          <img src="../imgs/project_detail/verticle2.jpg" alt="Arturo Ui Performance">
        </div>
      </div>
    </section>

    <!-- 리뷰 섹션 -->
    <section class="reviews-section">
      <h2>REVIEWS</h2>
      <div class="reviews-grid">
        <div class="review-item">
          <blockquote>
            „Eine gelungene expressiv körperbetonte Interpretation – die von der anfänglichen Lächerlichkeit des Unbeholfenen, zur erschreckend blutrünstigen Unmenschlichkeit entartet. Eine fantastische Umsetzung eines erschreckend aktuellen Stoffs."
          </blockquote>
          <cite>Reichenhaller Tagblatt</cite>
        </div>
        <div class="review-item">
          <blockquote>
            „Der Abend enthält starke Szenen, etwa den Rhetorik-Unterricht. Diesen nimmt Ui bei einem Schauspieler, köstlich gespielt von Michael Maertens in Form einer Videoeinblendung. Treffsicher die Persiflage auf politische Theatralik."
          </blockquote>
          <cite>Salzburger Nachrichten</cite>
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
  padding: 6rem 2rem 1rem 2rem;
  max-width: 1200px;
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

/* 이미지 분할 블록 */
.image-split {
  padding: 0 2rem 3rem 2rem;
  max-width: 1200px;
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
  max-width: 1200px;
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
  max-width: 500px;
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
  max-width: 1200px;
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
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.review-item blockquote {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #555;
  margin: 0 0 1.5rem 0;
  font-style: italic;
  border-left: 3px solid #8B0000;
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
