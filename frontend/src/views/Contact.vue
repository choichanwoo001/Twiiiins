<template>
  <div class="contact">
    <!-- 텍스트 섹션 -->
    <section class="contact-text">
      <h1>CONTACT</h1>
    </section>

    <!-- 콘텐츠 섹션 -->
    <section class="contact-content">
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
    </section>
  </div>
</template>

<style scoped>
.contact {
  background-color: white;
  color: #333;
  font-family: 'Arial', sans-serif;
  min-height: calc(100vh - 60px); /* 헤더 높이 제외 */
  margin-top: -60px; /* 헤더 패딩 상쇄 */
  padding-top: 60px; /* 헤더 높이만큼 상단 패딩 */
  display: flex;
}

/* 텍스트 섹션 */
.contact-text {
  width: 50%;
  padding: 4rem 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

h1 {
  font-size: 4rem;
  font-weight: bold;
  color: #8B4513;
  margin: 0;
  text-align: left;
  letter-spacing: 3px;
  writing-mode: vertical-rl;
  text-orientation: mixed;
}

/* 콘텐츠 섹션 */
.contact-content {
  width: 50%;
  padding: 4rem 2rem;
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

/* 반응형 디자인 */
@media (max-width: 768px) {
  .contact {
    flex-direction: column;
  }
  
  .contact-text,
  .contact-content {
    width: 100%;
    padding: 2rem 1rem;
  }
  
  h1 {
    font-size: 2.5rem;
    writing-mode: horizontal-tb;
    text-orientation: initial;
    text-align: center;
  }
  
  .contact-content {
    gap: 2rem;
  }
  
  .intro-text {
    font-size: 0.8rem;
    text-align: center;
  }
  
  .contact-details {
    gap: 1.5rem;
  }
  
  .contact-item {
    text-align: center;
  }
  
  .download-section {
    margin-top: 1rem;
  }
  
  .download-section h2 {
    text-align: center;
  }
}

@media (max-width: 480px) {
  h1 {
    font-size: 2rem;
  }
  
  .contact-name {
    font-size: 1rem;
  }
  
  .contact-email {
    font-size: 0.9rem;
  }
  
  .download-item span {
    font-size: 0.9rem;
  }
}
</style>
