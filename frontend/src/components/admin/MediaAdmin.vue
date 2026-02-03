<template>
  <div class="content-section">
    <h1 class="section-title">Media 관리</h1>
    
    <!-- Media 탭 -->
    <div class="tabs">
      <button 
        :class="['tab', { active: activeTab === 'music' }]"
        @click="activeTab = 'music'">
        Music
      </button>
      <button 
        :class="['tab', { active: activeTab === 'videos' }]"
        @click="activeTab = 'videos'">
        Videos
      </button>
      <button 
        :class="['tab', { active: activeTab === 'photos' }]"
        @click="activeTab = 'photos'">
        Photos
      </button>
      <button 
        :class="['tab', { active: activeTab === 'news' }]"
        @click="activeTab = 'news'">
        News
      </button>
      <button 
        :class="['tab', { active: activeTab === 'equipment' }]"
        @click="activeTab = 'equipment'">
        Equipment
      </button>
    </div>

    <!-- 탭 콘텐츠 -->
    <div class="tab-content">
      <MusicTab v-if="activeTab === 'music'" />
      <VideoTab v-if="activeTab === 'videos'" />
      <PhotoTab v-if="activeTab === 'photos'" />
      <NewsTab v-if="activeTab === 'news'" />
      <EquipmentTab v-if="activeTab === 'equipment'" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import MusicTab from './tabs/MusicTab.vue'
import VideoTab from './tabs/VideoTab.vue'
import PhotoTab from './tabs/PhotoTab.vue'
import NewsTab from './tabs/NewsTab.vue'
import EquipmentTab from './tabs/EquipmentTab.vue'

const activeTab = ref('music')
</script>

<style scoped>
.content-section {
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #1E1D1D;
}

.tabs {
  display: flex;
  border-bottom: 2px solid var(--color-border);
  margin-bottom: 2rem;
}

.tab {
  padding: 1rem 2rem;
  background: none;
  border: none;
  font-size: 1rem;
  font-weight: 500;
  color: var(--color-text-light);
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 3px solid transparent;
}

.tab:hover {
  color: #1E1D1D;
  background: #f8f9fa;
}

.tab.active {
  color: #007bff;
  border-bottom-color: #007bff;
}

.tab-content {
  min-height: 20rem;
}
</style>
