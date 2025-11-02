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
    <div class="sidebar-footer">
      <button class="logout-button" @click="handleLogout">
        로그아웃
      </button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '../../services/authService'

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
const router = useRouter()

const handleMenuClick = (sectionKey) => {
  emit('update:activeSection', sectionKey)
}

const handleLogout = () => {
  logout()
  router.push('/login')
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

.sidebar-footer {
  position: absolute;
  bottom: 2rem;
  left: 0;
  right: 0;
  padding: 0 2rem;
}

.logout-button {
  width: 100%;
  padding: 0.75rem;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: background-color 0.2s;
}

.logout-button:hover {
  background: #c0392b;
}
</style>
