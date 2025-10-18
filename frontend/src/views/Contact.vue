<template>
  <div class="contact">
    <!-- 좌측 고정 타이틀 -->
    <aside class="contact-title">
      <h1>CONTACT</h1>
    </aside>

    <!-- 우측 콘텐츠 -->
    <main class="contact-content">
      <!-- 안내 문구 -->
      <div class="contact-info">
        <p class="intro-text">
          FOR COLLABORATIONS, PERFORMANCE INQUIRIES, OR PORTFOLIO REQUESTS,<br>
          PLEASE CONTACT US HERE. WE LOOK FORWARD TO CONNECTING WITH YOU!
        </p>
      </div>

      <!-- 연락처 정보 -->
      <div class="contact-details">
        <div class="contact-item">
          <div class="contact-name">TWIIIINS</div>
          <div class="contact-email">stickypeanutbutter@proton.me</div>
        </div>

        <div class="contact-item">
          <div class="contact-name">Dowon Kim</div>
          <div class="contact-role">Violin</div>
          <div class="contact-email">kimdowon.c1@gmail.com</div>
        </div>

        <div class="contact-item">
          <div class="contact-name">Linus Shastri</div>
          <div class="contact-role">Baritone Violin</div>
          <div class="contact-email">linusshastri@protonmail.com</div>
        </div>
      </div>

      <!-- 다운로드 섹션 -->
      <div class="download-section">
        <h2>Download</h2>
        <div class="download-list">
          <div class="download-item">
            <span>Portfolio [EN]</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M12 19L7 14M12 19L17 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="download-item">
            <span>Portfolio [DE]</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M12 19L7 14M12 19L17 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="download-item">
            <span>Portfolio [KR]</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M12 19L7 14M12 19L17 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="download-item">
            <span>Stage Rider</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M12 19L7 14M12 19L17 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <div class="download-item">
            <span>CV</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 5V19M12 19L7 14M12 19L17 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 레이아웃 */
.contact {
  background: #fff;
  color: #222;
  min-height: calc(100vh - 60px);
  margin-top: -60px;
  padding-top: calc(60px + 6rem);
  display: grid;
  grid-template-columns: 1fr 2.2fr;
  align-items: start;
  gap: 4rem;
  margin-left: auto;
  margin-right: auto;
  padding-left: 2rem;
  padding-right: 4rem;
}

/* 좌측 타이틀 */
.contact-title {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding-top: 2rem;
}

.contact-title h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 500;
  letter-spacing: 0.12em;
  color: #8B4513;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
}

/* 우측 콘텐츠 */
.contact-content {
  max-width: 800px;
  width: 100%;
  margin-right: 2rem;
  justify-self: end;
  padding-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 3rem;
}

/* 안내 문구 */
.contact-info {
  margin-bottom: 2rem;
}

.intro-text {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

/* 연락처 정보 */
.contact-details {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.contact-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.contact-name {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}

.contact-role {
  font-size: 0.9rem;
  color: #666;
  font-style: italic;
}

.contact-email {
  font-size: 1rem;
  color: #555;
}

/* 다운로드 섹션 */
.download-section {
  margin-top: 2rem;
}

.download-section h2 {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
  margin: 0 0 1.5rem 0;
}

.download-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.download-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: color 0.3s ease;
}

.download-item:hover {
  color: #666;
}

.download-item span {
  font-size: 1rem;
  color: #555;
}

.download-item svg {
  color: #999;
  transition: color 0.3s ease;
}

.download-item:hover svg {
  color: #666;
}

</style>
