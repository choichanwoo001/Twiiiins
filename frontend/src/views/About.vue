<template>
  <div class="about">
    <!-- 왼쪽: 타이틀 영역 -->
    <div class="about-title-section">
      <h1>ABOUT</h1>
    </div>

    <!-- 오른쪽: 콘텐츠 영역 -->
    <div class="about-content-section">
      <!-- 텍스트 섹션 -->
      <div class="about-content" :lang="language">
        <div class="about-copy">
          <p v-for="paragraph in content" :key="paragraph">{{ paragraph }}</p>
        </div>
        <div class="language-switch" role="group" aria-label="About language / Sprache">
          <button v-for="option in ['en', 'de']" :key="option" type="button"
            :aria-pressed="language === option" :lang="option"
            :aria-label="option === 'en' ? 'English' : 'Deutsch'"
            @click="language = option">{{ option.toUpperCase() }}</button>
        </div>
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
              <img src="../imgs/About/20250719-P1250816 1.png" alt="TWIIIINS Performance" loading="lazy">
            </div>
            <div class="image-item">
              <img src="../imgs/About/20250719-P1250891 1.png" alt="TWIIIINS Performance" loading="lazy">
            </div>
          </div>
          <!-- 두 번째 행 -->
          <div class="image-row">
            <div class="image-item">
              <img src="../imgs/About/unnamed 1.png" alt="TWIIIINS Performance" loading="lazy">
            </div>
            <div class="image-item">
              <img src="../imgs/About/TwinsMusicApril2024-9 1.png" alt="TWIIIINS Performance" loading="lazy">
            </div>
            <div class="image-item">
              <img src="../imgs/About/TwinsMusicApril2024-6 1.png" alt="TWIIIINS Performance" loading="lazy">
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const language = ref('en')
const translations = {
  "en": [
    "TWIIIINS are a modern violin duo from South Korea and Germany. Their setup consists of two violins, one in standard tuning and one baritone violin tuned an octave lower. Loop stations and effect pedals turn them into basslines, kick drums, distorted riffs and wah-wah figures, with two-part vocals woven in throughout. What sounds like a full band is built layer by layer in front of the audience. The set moves between original songs and their own arrangements of well-known titles, ranging from folk-pop to classical, with influences from jazz, funk, rock and film music. That is exactly what draws Dowon Kim and Linus Shastri to it: hearing an instrument you think you know in an entirely different role.",
    "Both have played classical violin since childhood, and both took an early interest in other instruments and in music production. They met in Sweden in 2022 during an international programme on social entrepreneurship, where one of their first songs together was written and named after the room it came from: “Common Room”. A year later they moved to Salzburg and built the duo there. The name comes from Two Violins; the four i’s stand for the instrument’s four strings. Their songs grow out of what they experience together — “Deep Into Cold Water” was written after the two of them jumped into the Eisbach in Salzburg. They work in blocks, each bringing their own ideas, trying things out side by side. Today they live in Linz and Munich, where Kim studies jazz violin and Shastri studies osteopathy.",
    "In 2025 TWIIIINS composed the stage music for “The Resistible Rise of Arturo Ui” at Salzburger Landestheater and appeared on stage themselves in all 13 performances. They have played Herzberg Festival and Kirchheimer Musiknacht twice each, Flensburger Hofkultur and the gala show of the Tübingen Juggling Convention. In July 2026 they toured South Korea with five self-organised concerts, including Seongsu Art Hall in Seoul. Their debut album “Only The Beginning” was released in 2026, following the singles “Common Room” (2023) and “Time” (2024)."
  ],
  "de": [
    "TWIIIINS sind ein modernes Violinduo aus Südkorea und Deutschland. Auf der Bühne stehen zwei Geigen, eine in Standardstimmung und eine eine Oktave tiefer gestimmte Baritonvioline. Über Loopstations und Effektpedale werden daraus Basslines, Kickdrums, verzerrte Riffs und Wah-Wah-Figuren, dazwischen immer wieder zweistimmiger Gesang. Was klingt wie eine ganze Band, entsteht Schicht für Schicht vor den Augen des Publikums. Gespielt werden eigene Songs und eigene Arrangements bekannter Titel, von Folk-Pop bis Klassik, mit Einflüssen aus Jazz, Funk, Rock und Filmmusik. Genau darin liegt für Dowon Kim und Linus Shastri der Reiz: ein Instrument, das man zu kennen glaubt, plötzlich in einer anderen Rolle zu hören.",
    "Beide spielen seit ihrer Kindheit klassische Geige, beide interessierten sich früh für andere Instrumente und für Musikproduktion. Kennengelernt haben sie sich 2022 in Schweden, während eines internationalen Studienprogramms zu Social Entrepreneurship. Dort entstand auch einer ihrer ersten gemeinsamen Songs, benannt nach dem Raum, in dem sie ihn schrieben: „Common Room“. Ein Jahr später zogen beide nach Salzburg und bauten das Duo dort auf. Der Name steht für Two Violins, die vier i für die vier Saiten des Instruments. Ihre Songs entstehen aus dem, was sie gemeinsam erleben — „Deep Into Cold Water“ schrieben sie, nachdem sie in den Salzburger Eisbach gesprungen waren. Gearbeitet wird in Blöcken, jeder bringt eigene Ideen mit, ausprobiert wird zusammen. Heute leben die beiden in Linz und München, wo Kim Jazzvioline und Shastri Osteopathie studiert.",
    "2025 komponierten TWIIIINS die Bühnenmusik zu „Der aufhaltsame Aufstieg des Arturo Ui“ am Salzburger Landestheater und standen in 13 Vorstellungen selbst auf der Bühne. Sie spielten zweimal beim Herzberg Festival und bei der Kirchheimer Musiknacht, bei der Flensburger Hofkultur und in der Galashow der Tübinger Jonglierconvention. Im Juli 2026 tourten sie mit fünf selbst organisierten Konzerten durch Südkorea, darunter die Seongsu Art Hall in Seoul. Nach den Singles „Common Room“ (2023) und „Time“ (2024) erschien 2026 das Debütalbum „Only The Beginning“."
  ]
}
const content = computed(() => translations[language.value])
</script>

<style scoped>
.about {
  background-color: white;
  color: #1E1D1D;
  font-family: 'Jost', sans-serif;
  /* Keep the internal desktop scroll area above the fixed social links. */
  height: calc(100dvh - var(--navbar-height) - 4rem);
  overflow-y: auto;
  overflow-x: hidden;
  padding-top: 3rem;
  padding-left: 2rem;
  padding-right: 4rem;
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
  display: grid;
  width: 100%;
  max-width: 50rem;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
  gap: 2rem;
  text-align: left;
}

.about-copy {
  min-width: 0;
}

.language-switch {
  display: flex;
  gap: 0.8rem;
  align-items: center;
}

.language-switch button {
  padding: 0.25rem 0.1rem;
  min-height: 2.75rem;
  border: 0;
  background: transparent;
  color: #777;
  font: inherit;
  font-size: 0.8rem;
  letter-spacing: 0.06em;
  cursor: pointer;
  transition: color 0.15s ease;
}

.language-switch button[aria-pressed='true'],
.language-switch button:hover {
  color: #8b4513;
}

.language-switch button:focus-visible {
  outline: 1px solid #8b4513;
  outline-offset: 3px;
}

.about-content p {
  font-size: 1.1rem;
  line-height: 1.8;
  color: var(--color-text-secondary); /* #555 */
  margin-bottom: 1.5rem;
  overflow-wrap: break-word;
  hyphens: auto;
  font-weight: 400;
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
    padding-top: 4rem;
    padding-left: 1.5rem; /* 모바일 여백 조정 */
    padding-right: 1.5rem;
    height: auto; /* 모바일에서는 자연스러운 스크롤 허용 */
    overflow-y: visible;
    padding-bottom: 4rem; /* 하단 여백 추가 */
  }

  .about-title-section {
    padding-top: 0;
    margin-bottom: 2rem;
  }

  .about-content-section {
    padding-top: 0;
    gap: 2rem;
  }

  .about-content {
    grid-template-columns: minmax(0, 1fr);
    gap: 0.75rem;
  }

  .language-switch {
    grid-row: 1;
    justify-self: end;
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
