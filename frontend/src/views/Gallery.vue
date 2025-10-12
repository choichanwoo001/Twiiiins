<template>
  <div class="gallery container">
    <h1>Gallery</h1>
    
    <div class="filters">
      <button @click="filterType = null" :class="{ active: filterType === null }">전체</button>
      <button @click="filterType = 'VIDEO'" :class="{ active: filterType === 'VIDEO' }">영상</button>
      <button @click="filterType = 'IMAGE'" :class="{ active: filterType === 'IMAGE' }">사진</button>
    </div>

    <div class="grid" v-if="filteredMedia.length > 0">
      <div v-for="item in filteredMedia" :key="item.id" class="card">
        <img :src="item.thumbnailUrl || item.url" :alt="item.title">
        <div class="card-content">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
          <span class="badge">{{ item.type }}</span>
        </div>
      </div>
    </div>
    <div v-else class="loading">미디어를 불러오는 중...</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import apiClient from '../api/axios'

const media = ref([])
const filterType = ref(null)

const filteredMedia = computed(() => {
  if (!filterType.value) return media.value
  return media.value.filter(item => item.type === filterType.value)
})

onMounted(async () => {
  try {
    const res = await apiClient.get('/media')
    media.value = res.data
  } catch (error) {
    console.error('미디어 로딩 실패:', error)
  }
})
</script>

<style scoped>
.gallery {
  padding: 2rem 0;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 2rem;
  text-align: center;
}

.filters {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.filters button {
  padding: 0.5rem 1.5rem;
  border: 2px solid #000;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s;
}

.filters button:hover,
.filters button.active {
  background: #000;
  color: #fff;
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #000;
  color: #fff;
  border-radius: 12px;
  font-size: 0.8rem;
  margin-top: 0.5rem;
}
</style>

