<template>
  <div id="app">
    <!-- 관리자 페이지가 아닐 때만 네비게이션 표시 -->
    <nav v-if="!isAdminPage" class="navbar">
      <div class="container">
        <router-link to="/" class="logo">TWIIIINS</router-link>
        <div class="nav-links">
          <router-link to="/about">ABOUT</router-link>
          <router-link to="/projects">PROJECTS</router-link>
          <router-link to="/concerts">CONCERTS</router-link>
          <router-link to="/media">MEDIA</router-link>
          <router-link to="/shop">SHOP</router-link>
          <router-link to="/contact">CONTACT</router-link>
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
let scrollContainer = null
let scrollListener = null

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
  
  // about 페이지에서는 스크롤을 내렸을 때만 표시
  if (isAboutPage.value) {
    return isAllowedPath && scrollY.value > 100
  }
  
  return isAllowedPath
})

const updateScrollUpVisibility = () => {
  const { scrollHeight, clientHeight } = document.documentElement
  const isScrollable = scrollHeight > clientHeight + 1
  const hasScrolled = window.scrollY > 100
  showScrollUp.value = isScrollable && hasScrolled
}

const updateScrollY = () => {
  if (isAboutPage.value && scrollContainer) {
    // about 페이지의 스크롤 컨테이너에서 스크롤 위치 확인
    scrollY.value = scrollContainer.scrollTop
  } else {
    // 다른 페이지에서는 window 스크롤 확인
    scrollY.value = window.scrollY
  }
}

const setupAboutPageScroll = () => {
  // 기존 리스너 제거
  if (scrollListener && scrollContainer) {
    scrollContainer.removeEventListener('scroll', scrollListener)
  }
  
  // about 페이지인 경우 스크롤 컨테이너 찾기
  if (isAboutPage.value) {
    nextTick(() => {
      // .about 클래스를 가진 요소 찾기
      const aboutElement = document.querySelector('.about')
      if (aboutElement) {
        scrollContainer = aboutElement
        scrollListener = () => updateScrollY()
        scrollContainer.addEventListener('scroll', scrollListener, { passive: true })
        updateScrollY() // 초기값 설정
      }
    })
  } else {
    scrollContainer = null
    scrollListener = null
  }
}

// 라우트 변경 감지
watch(() => route.path, () => {
  setupAboutPageScroll()
}, { immediate: true })

onMounted(() => {
  // 앱 스토어 초기화
  appStore.initialize()
  
  updateScrollUpVisibility()
  updateScrollY()
  window.addEventListener('scroll', updateScrollUpVisibility, { passive: true })
  window.addEventListener('resize', updateScrollUpVisibility)
  
  // about 페이지 스크롤 설정
  setupAboutPageScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateScrollUpVisibility)
  window.removeEventListener('resize', updateScrollUpVisibility)
  
  // about 페이지 스크롤 리스너 제거
  if (scrollListener && scrollContainer) {
    scrollContainer.removeEventListener('scroll', scrollListener)
  }
})

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}
</script>

<style>
/* 기본 스타일은 style.css에서 관리 */
</style>

