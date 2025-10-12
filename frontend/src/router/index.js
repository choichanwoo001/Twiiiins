import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import About from '../views/About.vue'
import Projects from '../views/Projects.vue'
import ProjectDetail from '../views/ProjectDetail.vue'
import Concert from '../views/Concert.vue'
import Media from '../views/Media.vue'
import Shop from '../views/Shop.vue'
import Contact from '../views/Contact.vue'
import Admin from '../views/Admin.vue'

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
    path: '/projects/arturo-ui',
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
    path: '/admin',
    name: 'Admin',
    component: Admin
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

