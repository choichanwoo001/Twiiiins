<template>
  <aside class="admin-sidebar" role="navigation" aria-label="관리자 메뉴">
    <h2 class="sidebar-title">TWIIIINS <span>관리자</span></h2>
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
    label: '대시보드',
    ariaLabel: '대시보드 메뉴'
  },
  {
    key: 'concerts',
    label: '공연',
    ariaLabel: '콘서트 관리 메뉴'
  },
  {
    key: 'media',
    label: '미디어',
    ariaLabel: '미디어 관리 메뉴'
  },
  {
    key: 'newsletter',
    label: '뉴스레터',
    ariaLabel: '뉴스레터 관리 메뉴'
  },
  {
    key: 'translations',
    label: '자동 번역',
    ariaLabel: '자동 번역 관리 메뉴'
  },
  {
    key: 'projects',
    label: '프로젝트',
    ariaLabel: '프로젝트 관리 메뉴'
  },
  {
    key: 'contact',
    label: '연락처',
    ariaLabel: '연락처 관리 메뉴'
  },
  {
    key: 'download-files',
    label: '다운로드',
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
  background: #10263b;
  color: white;
  padding: 2rem 0;
  position: fixed;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.sidebar-title {
  font-size: 1.15rem;
  font-weight: bold;
  text-align: center;
  margin-bottom: 2rem;
  padding: 0 1rem;
}
.sidebar-title span { display: block; margin-top: .3rem; color: #a9c6dc; font-size: .8rem; }

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: .25rem;
  padding: 0 .75rem;
}

.nav-item {
  padding: .8rem 1rem;
  background: none;
  border: none;
  color: #c8d9e7;
  border-radius: .45rem;
  text-align: left;
  cursor: pointer;
  font-size: 1rem;
}

.nav-item:hover {
  background: #203d55;
  color: white;
}

.nav-item.active {
  background: #d4e7f5;
  color: #10263b;
  font-weight: 700;
}

.sidebar-footer {
  margin-top: auto;
  padding: 1.5rem 1rem 0;
}

.logout-button {
  width: 100%;
  padding: 0.75rem;
  background: transparent;
  color: white;
  border: 1px solid #7090aa;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: background-color 0.2s;
}

.logout-button:hover {
  background: #203d55;
}
@media (max-width: 768px) {
  .admin-sidebar { position: relative; width: 100%; height: auto; padding: 1rem 0; box-sizing: border-box; }
  .sidebar-title { margin-bottom: 1rem; }
  .sidebar-nav { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); }
  .nav-item { padding: .65rem .15rem; text-align: center; font-size: .85rem; }
  .sidebar-footer { padding: .8rem 1rem 0; text-align: right; }
  .logout-button { width: auto; }
}
</style>
