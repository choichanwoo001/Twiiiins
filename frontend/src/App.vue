<template>
  <div id="app">
    <!-- 관리자 페이지가 아닐 때만 네비게이션 표시 -->
    <nav v-if="!isAdminPage" class="navbar">
      <div class="container">
        <router-link to="/" class="logo">TWIIIINS</router-link>
        
        <!-- 데스크탑 메뉴 -->
        <div class="nav-links desktop-nav">
          <router-link to="/about">ABOUT</router-link>
          <router-link to="/projects">PROJECTS</router-link>
          <router-link to="/concerts">CONCERTS</router-link>
          <router-link to="/media">MEDIA</router-link>
          <router-link to="/shop">SHOP</router-link>
          <router-link to="/contact">CONTACT</router-link>
        </div>

        <!-- 햄버거 버튼 (모바일) -->
        <button class="hamburger-button" @click="toggleMobileMenu" :class="{ 'is-open': isMobileMenuOpen }">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>

      <!-- 모바일 메뉴 오버레이 -->
      <div class="mobile-menu-overlay" :class="{ 'is-open': isMobileMenuOpen }">
        <div class="mobile-nav-links">
          <router-link to="/about" @click="closeMobileMenu">ABOUT</router-link>
          <router-link to="/projects" @click="closeMobileMenu">PROJECTS</router-link>
          <router-link to="/concerts" @click="closeMobileMenu">CONCERTS</router-link>
          
          <!-- MEDIA (Accordion) -->
          <div class="mobile-menu-item" :class="{ active: isMediaMenuOpen }" @click="toggleMediaMenu">MEDIA</div>
          <div v-show="isMediaMenuOpen" class="mobile-sub-menu">
            <router-link to="/media?section=music" class="mobile-sub-link" @click.stop="closeMobileMenu">MUSIC</router-link>
            <router-link to="/media?section=videos" class="mobile-sub-link" @click.stop="closeMobileMenu">VIDEOS</router-link>
            <router-link to="/media?section=photos" class="mobile-sub-link" @click.stop="closeMobileMenu">PHOTOS</router-link>
            <router-link to="/media?section=news" class="mobile-sub-link" @click.stop="closeMobileMenu">NEWS</router-link>
            <router-link to="/media?section=equipment" class="mobile-sub-link" @click.stop="closeMobileMenu">EQUIPMENT</router-link>
          </div>
          <router-link to="/shop" @click="closeMobileMenu">SHOP</router-link>
          <router-link to="/contact" @click="closeMobileMenu">CONTACT</router-link>
        </div>
      </div>
    </nav>
    
    <main>
      <router-view />
    </main>
    
    <!-- SNS 링크 (왼쪽 하단) - 메인 페이지들에 표시 -->
    <div v-if="shouldShowSnsLinks" class="sns-links">
      <a href="https://www.instagram.com/twiiiinsmusic/?hl=en" target="_blank" rel="noopener noreferrer" class="sns-link">INSTAGRAM</a>
      <a href="https://www.youtube.com/@twiiiins" target="_blank" rel="noopener noreferrer" class="sns-link">YOUTUBE</a>
      <a href="https://open.spotify.com/artist/5W2MPoNirjyFmHlBqN8uxZ?si=pWbiWGSrTM-DUDKLd-qwyw" target="_blank" rel="noopener noreferrer" class="sns-link">SPOTIFY</a>
      <a href="https://music.apple.com/kr/artist/twiiiins/1717657113?l=en-GB" target="_blank" rel="noopener noreferrer" class="sns-link">APPLE MUSIC</a>
    </div>

    <!-- 스크롤 업 버튼 (오른쪽 하단) - 관리자 페이지가 아닐 때만 표시 -->
    <button v-if="!isAdminPage && showScrollUp" class="scroll-up-button" @click="scrollToTop" aria-label="Scroll to top">
      ↑
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from './stores'

const route = useRoute()  
const appStore = useAppStore()
const showScrollUp = ref(false)
const scrollY = ref(0)
const isBottomReached = ref(false) // 바닥 도달 여부
const isMobileMenuOpen = ref(false) // 모바일 메뉴 상태
let scrollContainer = null
let scrollListener = null

const isMobile = ref(false)
const resizeObserver = ref(null)

// 현재 페이지가 관리자 페이지인지 확인
const isAdminPage = computed(() => route.path.startsWith('/admin'))

// 현재 페이지가 홈 페이지인지 확인
const isHomePage = computed(() => route.path === '/')

// 현재 페이지가 about 페이지인지 확인
const isAboutPage = computed(() => route.path === '/about')

// SNS 링크를 표시할 페이지들 (홈 + 메인 페이지들)
const shouldShowSnsLinks = computed(() => {
  const pathsWithSnsLinks = ['/', '/about', '/projects', '/concerts', '/media', '/shop', '/contact']
  const isAllowedPath = pathsWithSnsLinks.includes(route.path) && !route.path.startsWith('/admin')
  
  // 조건 없이 항상 표시
  return isAllowedPath
})

const updateScrollUpVisibility = () => {
  const { scrollHeight, clientHeight } = document.documentElement
  const isScrollable = scrollHeight > clientHeight + 1
  const hasScrolled = window.scrollY > 100
  showScrollUp.value = isScrollable && hasScrolled
}

const updateScrollY = () => {
  let currentScrollTop = 0
  let currentClientHeight = 0
  let currentScrollHeight = 0

  // About 페이지이면서 데스크탑일 때만 내부 스크롤 컨테이너 사용
  if (isAboutPage.value && !isMobile.value && scrollContainer) {
    // about 페이지의 스크롤 컨테이너에서 스크롤 위치 확인
    currentScrollTop = scrollContainer.scrollTop
    currentClientHeight = scrollContainer.clientHeight
    currentScrollHeight = scrollContainer.scrollHeight
    
    scrollY.value = currentScrollTop
  } else {
    // 다른 페이지거나 모바일 About 페이지는 window 스크롤 확인
    currentScrollTop = window.scrollY
    currentClientHeight = window.innerHeight
    currentScrollHeight = document.documentElement.scrollHeight
    
    scrollY.value = currentScrollTop
  }

  // 바닥 도달 체크 (여유값 150px)
  const threshold = 150
  if (currentScrollHeight <= currentClientHeight) {
    // 스크롤이 없는 경우 (컨텐츠가 짧음) -> 항상 바닥으로 간주
    isBottomReached.value = true
  } else {
    isBottomReached.value = (currentScrollTop + currentClientHeight) >= (currentScrollHeight - threshold)
  }
}

const setupAboutPageScroll = () => {
  // 기존 리스너 제거
  if (scrollListener && scrollContainer) {
    scrollContainer.removeEventListener('scroll', scrollListener)
  }
  
  scrollContainer = null
  scrollListener = null

  // about 페이지인 경우
  if (isAboutPage.value) {
    nextTick(() => {
      // 데스크탑일 때만 .about 컨테이너의 스크롤 감지
      if (!isMobile.value) {
        const aboutElement = document.querySelector('.about')
        if (aboutElement) {
          scrollContainer = aboutElement
          scrollListener = () => {
            updateScrollY()
          }
          scrollContainer.addEventListener('scroll', scrollListener, { passive: true })
        }
      }
      // 모바일이거나 컨테이너를 못 찾은 경우엔 window 스크롤(handleWindowScroll)이 처리함
      updateScrollY() // 초기값 설정
    })
  } else {
    updateScrollY() // 초기값 설정 (window 기준)
  }
}

const isMediaMenuOpen = ref(false) // 미디어 서브메뉴 토글 상태

// 모바일 메뉴 토글
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
  if (isMobileMenuOpen.value) {
    document.body.style.overflow = 'hidden' // 메뉴 열릴 때 스크롤 방지
  } else {
    document.body.style.overflow = ''
    isMediaMenuOpen.value = false // 메뉴 닫을 때 서브메뉴도 초기화
  }
}

// 미디어 메뉴 토글
const toggleMediaMenu = () => {
  isMediaMenuOpen.value = !isMediaMenuOpen.value
}

// 메뉴 링크 클릭 시 닫기
const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
  isMediaMenuOpen.value = false
  document.body.style.overflow = ''
}

// 라우트 변경 감지
watch(() => route.path, () => {
  setupAboutPageScroll()
  closeMobileMenu() // 페이지 이동 시 메뉴 닫기
  // 라우트 변경 시 상태 초기화 및 재계산
  isBottomReached.value = false
  nextTick(() => {
    updateScrollY()
  })
}, { immediate: true })

// 윈도우 스크롤 핸들러 (통합)
const handleWindowScroll = () => {
  updateScrollUpVisibility()
  // About 페이지 데스크탑 모드가 아닐 때만 window 스크롤 업데이트
  if (!(isAboutPage.value && !isMobile.value)) {
    updateScrollY()
  }
}

const checkMobile = () => {
  const wasMobile = isMobile.value
  isMobile.value = window.innerWidth <= 768 // 48rem
  
  // 모바일/데스크탑 전환 시 스크롤 설정 재초기화
  if (wasMobile !== isMobile.value) {
    setupAboutPageScroll()
  }
  
  updateScrollUpVisibility()
  updateScrollY()
}

onMounted(() => {
  // 앱 스토어 초기화
  appStore.initialize()
  
  checkMobile()
  window.addEventListener('scroll', handleWindowScroll, { passive: true })
  window.addEventListener('resize', checkMobile)
  
  // ResizeObserver로 body 높이 변화 감지 (아코디언 등으로 컨텐츠 길이 변할 때 대응)
  resizeObserver.value = new ResizeObserver(() => {
    updateScrollY()
    updateScrollUpVisibility()
  })
  resizeObserver.value.observe(document.body)
  
  // about 페이지 스크롤 설정
  setupAboutPageScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleWindowScroll)
  window.removeEventListener('resize', checkMobile)
  
  if (resizeObserver.value) {
    resizeObserver.value.disconnect()
  }
  
  // about 페이지 스크롤 리스너 제거
  if (scrollListener && scrollContainer) {
    scrollContainer.removeEventListener('scroll', scrollListener)
  }
})

const scrollToTop = () => {
  if (isAboutPage.value && !isMobile.value && scrollContainer) {
    scrollContainer.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  } else {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}
</script>

<style>
/* 기본 스타일은 style.css에서 관리 */
/* style.css 인코딩 문제로 인한 모바일 오버라이드 스타일 추가 */
@media (max-width: 48rem) {
  .sns-link {
    font-size: 3.2vw !important; /* 글자 크기 더 축소 */
    font-weight: 300 !important;
    max-width: 100%;
  }
  
  /* SNS 링크 간격 조정 - App.vue style 태그 내에서는 .sns-links 접근이 제한적일 수 있으나 시도 */
  .sns-links {
    gap: 1rem !important; /* 간격 줄임 */
    flex-wrap: nowrap !important; /* 줄바꿈 방지 */
    
    /* 모바일 가시성 제어 - 항상 표시 */
    opacity: 1 !important;
    pointer-events: auto !important;
    transition: opacity 0.5s ease;
  }
  
  /* 바닥에 도달했을 때만 표시 (제거됨 - 항상 표시) */
  /*
  .sns-links.mobile-visible {
    opacity: 1;
    pointer-events: auto;
  }
  */
  
  /* 모바일 네비게이션 여백 조정 (로고/햄버거 위치) */
  .navbar .container {
    padding: 0 1.5rem !important; /* 양쪽 여백 늘림 */
  }

  /* style.css 인코딩 문제로 인한 로고 사이즈 오버라이드 */
  .logo {
    font-size: 2rem !important; /* 모바일 로고 축소 */
  }

  /* 모바일 메뉴 오버레이 스크롤 및 배경 스타일 */
  .mobile-menu-overlay {
    background: rgba(255, 255, 255, 0.45) !important; /* 반투명 배경 */
    backdrop-filter: blur(15px) !important; /* 블러 효과 */
    -webkit-backdrop-filter: blur(15px) !important; /* Safari 지원 */
    overflow-y: auto !important;
    scrollbar-width: none !important; /* Firefox */
    -ms-overflow-style: none !important; /* IE 10+ */
    
    /* 레이아웃 수정: 상단 잘림 방지 */
    display: flex !important;
    flex-direction: column !important;
    justify-content: flex-start !important; /* 중앙 정렬 대신 상단 정렬 */
    padding-top: 8rem !important; /* 상단 여백 확보 */
    padding-bottom: 4rem !important;
  }
  
  .mobile-menu-overlay::-webkit-scrollbar {
    display: none !important; /* Chrome, Safari, Opera */
  }

  /* 모바일 메뉴 컨텐츠 여백 확보 */
  .mobile-nav-links {
    padding: 0 !important; /* 오버레이 패딩으로 대체 */
    min-height: min-content !important;
    width: 100%;
  }

  /* MEDIA 토글 버튼 스타일 (링크와 동일하게) */
  .mobile-menu-item {
    font-size: 1.5rem !important;
    color: #1E1D1D !important;
    font-weight: 300 !important;
    letter-spacing: 0.1rem !important;
    cursor: pointer !important;
  }

  /* 모바일 메뉴 미디어 서브항목 스타일 */
  .mobile-sub-menu {
    /* v-show 동작을 위해 !important 제거 */
    display: flex;
    flex-direction: column;
    gap: 1rem;
    align-items: center;
    width: 100%;
  }
  
  .mobile-sub-link {
    font-size: 1rem !important; /* 메인 메뉴보다 작게 */
    font-weight: 300 !important;
    color: #888 !important; /* 약간 연하게 */
    text-decoration: none !important;
    cursor: pointer !important;
    transition: color 0.3s ease !important;
  }
  
  .mobile-sub-link:hover {
    color: #FBCE7B !important;
  }

  .mobile-menu-item.active {
    color: #1E1D1D !important;
  }
}
</style>

