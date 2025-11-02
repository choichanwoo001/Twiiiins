import { createRouter, createWebHistory } from 'vue-router'
import { useAppStore } from '../stores'
import Home from '../views/Home.vue'
import About from '../views/About.vue'
import Projects from '../views/Projects.vue'
import ProjectDetail from '../views/ProjectDetail.vue'
import Concert from '../views/Concert.vue'
import Media from '../views/Media.vue'
import Shop from '../views/Shop.vue'
import Contact from '../views/Contact.vue'
import Admin from '../views/Admin.vue'
import Login from '../views/Login.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path: '/projects',
    name: 'Projects',
    component: Projects
  },
  {
    path: '/projects/:slug',
    name: 'ProjectDetail',
    component: ProjectDetail
  },
  {
    path: '/concerts',
    name: 'Concert',
    component: Concert
  },
  {
    path: '/media',
    name: 'Media',
    component: Media
  },
  {
    path: '/shop',
    name: 'Shop',
    component: Shop
  },
  {
    path: '/contact',
    name: 'Contact',
    component: Contact
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 인증 가드
router.beforeEach((to, from, next) => {
  const appStore = useAppStore()
  
  // 저장된 사용자 정보 복원
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    try {
      appStore.setUser(JSON.parse(savedUser))
    } catch (e) {
      localStorage.removeItem('user')
    }
  }
  
  // 인증이 필요한 페이지인지 확인
  if (to.meta.requiresAuth) {
    if (!appStore.isAuthenticated) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  } else {
    // 로그인 페이지에서 이미 로그인된 경우 admin으로 리다이렉트
    if (to.name === 'Login' && appStore.isAuthenticated) {
      next({ name: 'Admin' })
    } else {
      next()
    }
  }
})

export default router

