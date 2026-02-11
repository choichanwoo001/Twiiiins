<template>
  <div id="app">
    <!-- 관리자 페이지가 아닐 때만 네비게이션 표시 -->
    <nav v-if="!isAdminPage" class="navbar">
      <div class="container">
        <router-link to="/" class="logo" :class="{ 'small': !isHomePage }">TWIIIINS</router-link>
        
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
      <a v-for="sns in SNS_LINKS" 
         :key="sns.name" 
         :href="sns.url" 
         target="_blank" 
         rel="noopener noreferrer" 
         class="sns-link">
        {{ sns.name }}
      </a>
    </div>

    <!-- 스크롤 업 버튼 (오른쪽 하단) - 관리자 페이지가 아닐 때만 표시 -->
    <button v-if="!isAdminPage && showScrollUp" class="scroll-up-button" @click="scrollToTop" aria-label="Scroll to top">
      ↑
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from './stores'
import { useAppScroll } from './composables/useAppScroll'
import { useMobile } from './composables/useMobile'
import { SNS_LINKS, PATHS_WITH_SNS_LINKS } from './constants'

const route = useRoute()  
const appStore = useAppStore()
const isMobileMenuOpen = ref(false) // 모바일 메뉴 상태
const isMediaMenuOpen = ref(false) // 미디어 서브메뉴 토글 상태
const { isMobile } = useMobile()

// 스크롤 관련 로직 Composable 사용
const { 
  showScrollUp, 
  isBottomReached, 
  scrollToTop, 
  setupAboutPageScroll,
  updateScrollUpVisibility,
  updateScrollY
} = useAppScroll(route, isMobile)

// 현재 페이지가 관리자 페이지인지 확인
const isAdminPage = computed(() => route.path.startsWith('/admin'))

// 현재 페이지가 홈인지 확인
const isHomePage = computed(() => route.path === '/')

// SNS 링크를 표시할 페이지들 (홈 + 메인 페이지들)
const shouldShowSnsLinks = computed(() => {
  const isAllowedPath = PATHS_WITH_SNS_LINKS.includes(route.path) && !route.path.startsWith('/admin')
  return isAllowedPath
})

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

// 라우트 변경 감지하여 메뉴 닫기
watch(() => route.path, () => {
  closeMobileMenu() 
})

onMounted(() => {
  // 앱 스토어 초기화
  appStore.initialize()
})
</script>

<style>
/* 기본 스타일은 style.css에서 관리 */
/* 모바일 오버라이드 스타일은 style.css로 이관됨 */
</style>


