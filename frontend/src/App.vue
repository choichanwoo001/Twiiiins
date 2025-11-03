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
    
    <!-- SNS 링크 (왼쪽 하단) - 홈 페이지에서만 표시 -->
    <div v-if="isHomePage" class="sns-links">
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from './stores'

const route = useRoute()
const appStore = useAppStore()
const showScrollUp = ref(false)

// 현재 페이지가 관리자 페이지인지 확인
const isAdminPage = computed(() => route.path.startsWith('/admin'))

// 현재 페이지가 홈 페이지인지 확인
const isHomePage = computed(() => route.path === '/')

const updateScrollUpVisibility = () => {
  const { scrollHeight, clientHeight } = document.documentElement
  const isScrollable = scrollHeight > clientHeight + 1
  const hasScrolled = window.scrollY > 100
  showScrollUp.value = isScrollable && hasScrolled
}

onMounted(() => {
  // 앱 스토어 초기화
  appStore.initialize()
  
  updateScrollUpVisibility()
  window.addEventListener('scroll', updateScrollUpVisibility, { passive: true })
  window.addEventListener('resize', updateScrollUpVisibility)
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateScrollUpVisibility)
  window.removeEventListener('resize', updateScrollUpVisibility)
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

