<template>
  <div id="app">
    <nav class="navbar">
      <div class="container">
        <router-link to="/" class="logo">TWIIIINS</router-link>
        <div class="nav-links">
          <router-link to="/about">ABOUT</router-link>
          <router-link to="/gallery">PROJECTS</router-link>
          <router-link to="/gallery">CONCERTS</router-link>
          <router-link to="/gallery">MEDIA</router-link>
          <router-link to="/store">SHOP</router-link>
          <router-link to="/about">CONTACT</router-link>
        </div>
      </div>
    </nav>
    
    <main>
      <router-view />
    </main>
    
    <!-- SNS 링크 (왼쪽 하단) -->
    <div class="sns-links">
      <a href="#" class="sns-link">INSTAGRAM</a>
      <a href="#" class="sns-link">YOUTUBE</a>
      <a href="#" class="sns-link">SPOTIFY</a>
      <a href="#" class="sns-link">APPLE MUSIC</a>
    </div>

    <!-- 스크롤 업 버튼 (오른쪽 하단) -->
    <button class="scroll-up-button" @click="scrollToTop">
      ↑
    </button>
  </div>
</template>

<script setup>
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

