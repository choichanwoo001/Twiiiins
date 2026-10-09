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
import Privacy from '../views/Privacy.vue'
const Admin = () => import('../views/Admin.vue')
import Login from '../views/Login.vue'
import NewsletterAction from '../views/NewsletterAction.vue'
import apiClient from '../api/axios'

const routes = [
  ...(import.meta.env.DEV && import.meta.env.VITE_DUMMY_DATA === 'true'
    ? [{ path: '/dev/newsletter', name: 'NewsletterDemo', meta: { hideSiteChrome: true }, component: () => import('../views/NewsletterDemo.vue') }] : []),
  { path: '/newsletter/confirm', name: 'NewsletterConfirm', component: NewsletterAction },
  { path: '/newsletter/unsubscribe', name: 'NewsletterUnsubscribe', component: NewsletterAction },
  { path: '/privacy', name: 'Privacy', component: Privacy },
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
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) return { el: to.hash, top: 128 }
    return savedPosition || { top: 0 }
  }
})

// 인증 가드
router.beforeEach(async (to) => {
  const appStore = useAppStore()
  if (!to.meta.requiresAuth && to.name !== 'Login') return true
  if (localStorage.getItem('token')) {
    try {
      const response = await apiClient.get('/auth/me')
      appStore.setUser(response.data.data)
      if (to.name === 'Login') return { name: 'Admin' }
      return true
    } catch { appStore.logout(); localStorage.removeItem('user') }
  }
  if (to.meta.requiresAuth) return { name: 'Login', query: { redirect: to.fullPath } }
  return true
})

export default router

