<template>
  <aside class="admin-sidebar" role="navigation" aria-label="관리자 메뉴">
    <h2 class="sidebar-title">ADMIN</h2>
    <nav class="sidebar-nav" role="tablist">
      <button 
        v-for="item in menuItems"
        :key="item.key"
        :class="['nav-item', { active: activeSection === item.key }]"
        @click="handleMenuClick(item.key)"
        role="tab"
        :aria-selected="activeSection === item.key"
        :aria-label="item.ariaLabel"
      >
        {{ item.label }}
      </button>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  activeSection: {
    type: String,
    required: true
  }
})

// Emits
const emit = defineEmits(['update:activeSection'])

// Menu items configuration
const menuItems = computed(() => [
  {
    key: 'dashboard',
    label: 'Dashboard',
    ariaLabel: '대시보드 메뉴'
  },
  {
    key: 'concerts',
    label: 'Concerts',
    ariaLabel: '콘서트 관리 메뉴'
  },
  {
    key: 'media',
    label: 'Media',
    ariaLabel: '미디어 관리 메뉴'
  },
  {
    key: 'contact',
    label: 'Contact',
    ariaLabel: '연락처 관리 메뉴'
  },
  {
    key: 'download-files',
    label: 'Download Files',
    ariaLabel: '다운로드 파일 관리 메뉴'
  }
])

// Methods
const handleMenuClick = (sectionKey) => {
  emit('update:activeSection', sectionKey)
}
</script>

<style scoped>
.admin-sidebar {
  width: 15rem;
  background: #2c3e50;
  color: white;
  padding: 2rem 0;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-title {
  font-size: 1.5rem;
  font-weight: bold;
  text-align: center;
  margin-bottom: 2rem;
  padding: 0 1rem;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
}

.nav-item {
  padding: 1rem 2rem;
  background: none;
  border: none;
  color: #bdc3c7;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 1rem;
}

.nav-item:hover {
  background: #34495e;
  color: white;
}

.nav-item.active {
  background: #3498db;
  color: white;
}
</style>
