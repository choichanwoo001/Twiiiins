<template>
  <div class="admin">
    <!-- 왼쪽 사이드바 -->
    <aside class="admin-sidebar">
      <h2 class="sidebar-title">ADMIN</h2>
      <nav class="sidebar-nav">
        <button 
          :class="['nav-item', { active: activeSection === 'dashboard' }]"
          @click="activeSection = 'dashboard'">
          Dashboard
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'projects' }]"
          @click="activeSection = 'projects'">
          Projects
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'concerts' }]"
          @click="activeSection = 'concerts'">
          Concerts
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'media' }]"
          @click="activeSection = 'media'">
          Media
        </button>
        <button 
          :class="['nav-item', { active: activeSection === 'contact' }]"
          @click="activeSection = 'contact'">
          Contact
        </button>
      </nav>
    </aside>

    <!-- 메인 콘텐츠 영역 -->
    <main class="admin-content">
      <!-- Dashboard -->
      <div v-if="activeSection === 'dashboard'" class="content-section">
        <h1 class="section-title">Dashboard</h1>
        <div class="dashboard-card">
          <h3>방문자 통계</h3>
          <p>Google Analytics를 사용하여 방문자 통계를 확인할 수 있습니다.</p>
          <p>Google Analytics 대시보드: <a href="https://analytics.google.com" target="_blank">https://analytics.google.com</a></p>
          <div class="stats-placeholder">
            <p>GA4 추적 코드를 index.html에 추가하세요.</p>
          </div>
        </div>
      </div>

      <!-- Projects 관리 -->
      <div v-if="activeSection === 'projects'" class="content-section">
        <div class="section-header">
          <h1 class="section-title">Projects 관리</h1>
          <button class="btn-primary" @click="showProjectForm = true; editingProject = null">
            + 새 프로젝트
          </button>
        </div>
        
        <!-- 프로젝트 목록 -->
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>제목</th>
                <th>장소</th>
                <th>날짜</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="project in projects" :key="project.id">
                <td>{{ project.title }}</td>
                <td>{{ project.location }}</td>
                <td>{{ project.premiereDate }}</td>
                <td>
                  <button class="btn-edit" @click="editProject(project)">수정</button>
                  <button class="btn-delete" @click="deleteProject(project.id)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 프로젝트 폼 -->
        <div v-if="showProjectForm" class="modal-overlay" @click.self="showProjectForm = false">
          <div class="modal-content">
            <h2>{{ editingProject ? '프로젝트 수정' : '새 프로젝트' }}</h2>
            <form @submit.prevent="saveProject">
              <div class="form-group">
                <label>제목</label>
                <input v-model="projectForm.title" required>
              </div>
              <div class="form-group">
                <label>부제목</label>
                <input v-model="projectForm.subtitle">
              </div>
              <div class="form-group">
                <label>날짜</label>
                <input type="date" v-model="projectForm.premiereDate">
              </div>
              <div class="form-group">
                <label>장소</label>
                <input v-model="projectForm.location">
              </div>
              <div class="form-group">
                <label>설명 1</label>
                <textarea v-model="projectForm.description1" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>설명 2</label>
                <textarea v-model="projectForm.description2" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>설명 3</label>
                <textarea v-model="projectForm.description3" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>메인 이미지 URL</label>
                <input v-model="projectForm.mainImageUrl">
              </div>
              <div class="form-group">
                <label>가로 이미지 1 URL</label>
                <input v-model="projectForm.horizontal1ImageUrl">
              </div>
              <div class="form-group">
                <label>가로 이미지 2 URL</label>
                <input v-model="projectForm.horizontal2ImageUrl">
              </div>
              <div class="form-group">
                <label>세로 이미지 1 URL</label>
                <input v-model="projectForm.vertical1ImageUrl">
              </div>
              <div class="form-group">
                <label>세로 이미지 2 URL</label>
                <input v-model="projectForm.vertical2ImageUrl">
              </div>
              <div class="form-group">
                <label>커버 이미지 URL</label>
                <input v-model="projectForm.coverImageUrl">
              </div>
              <div class="form-group">
                <label>더보기 URL</label>
                <input v-model="projectForm.moreInfoUrl">
              </div>
              <div class="form-group">
                <label>리뷰 1 텍스트</label>
                <textarea v-model="projectForm.review1Text" rows="2"></textarea>
              </div>
              <div class="form-group">
                <label>리뷰 1 출처</label>
                <input v-model="projectForm.review1Source">
              </div>
              <div class="form-group">
                <label>리뷰 2 텍스트</label>
                <textarea v-model="projectForm.review2Text" rows="2"></textarea>
              </div>
              <div class="form-group">
                <label>리뷰 2 출처</label>
                <input v-model="projectForm.review2Source">
              </div>
              <div class="form-group">
                <label>URL Slug</label>
                <input v-model="projectForm.urlSlug">
              </div>
              <div class="form-group">
                <label>표시 순서</label>
                <input type="number" v-model="projectForm.displayOrder">
              </div>
              <div class="form-actions">
                <button type="submit" class="btn-primary">저장</button>
                <button type="button" class="btn-secondary" @click="showProjectForm = false">취소</button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- Concerts 관리 -->
      <div v-if="activeSection === 'concerts'" class="content-section">
        <div class="section-header">
          <h1 class="section-title">Concerts 관리</h1>
          <button class="btn-primary" @click="showConcertForm = true; editingConcert = null">
            + 새 콘서트
          </button>
        </div>
        
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>날짜</th>
                <th>장소</th>
                <th>이름</th>
                <th>상태</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="concert in concerts" :key="concert.id">
                <td>{{ concert.date }}</td>
                <td>{{ concert.location }}</td>
                <td>{{ concert.name }}</td>
                <td>{{ concert.isPast ? '지난 공연' : '예정' }}</td>
                <td>
                  <button class="btn-edit" @click="editConcert(concert)">수정</button>
                  <button class="btn-delete" @click="deleteConcert(concert.id)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 콘서트 폼 -->
        <div v-if="showConcertForm" class="modal-overlay" @click.self="showConcertForm = false">
          <div class="modal-content">
            <h2>{{ editingConcert ? '콘서트 수정' : '새 콘서트' }}</h2>
            <form @submit.prevent="saveConcert">
              <div class="form-group">
                <label>날짜</label>
                <input type="date" v-model="concertForm.date" required>
              </div>
              <div class="form-group">
                <label>장소</label>
                <input v-model="concertForm.location" required>
              </div>
              <div class="form-group">
                <label>이름</label>
                <input v-model="concertForm.name" required>
              </div>
              <div class="form-group">
                <label>시작 시간</label>
                <input v-model="concertForm.startTime">
              </div>
              <div class="form-group">
                <label>티켓 정보 URL</label>
                <input v-model="concertForm.ticketInfo">
              </div>
              <div class="form-group">
                <label>전체 주소</label>
                <input v-model="concertForm.fullLocation">
              </div>
              <div class="form-group">
                <label>구글맵 URL</label>
                <input v-model="concertForm.googleMapUrl">
              </div>
              <div class="form-group">
                <label>협업 정보</label>
                <input v-model="concertForm.collaborationInfo">
              </div>
              <div class="form-group">
                <label>
                  <input type="checkbox" v-model="concertForm.isPast">
                  지난 공연
                </label>
              </div>
              <div class="form-actions">
                <button type="submit" class="btn-primary">저장</button>
                <button type="button" class="btn-secondary" @click="showConcertForm = false">취소</button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- Media 관리 -->
      <div v-if="activeSection === 'media'" class="content-section">
        <h1 class="section-title">Media 관리</h1>
        
        <!-- Media 탭 -->
        <div class="tabs">
          <button 
            :class="['tab', { active: mediaTab === 'music' }]"
            @click="mediaTab = 'music'">
            Music
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'videos' }]"
            @click="mediaTab = 'videos'">
            Videos
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'photos' }]"
            @click="mediaTab = 'photos'">
            Photos
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'news' }]"
            @click="mediaTab = 'news'">
            News
          </button>
          <button 
            :class="['tab', { active: mediaTab === 'equipment' }]"
            @click="mediaTab = 'equipment'">
            Equipment
          </button>
        </div>

        <!-- Music 탭 -->
        <div v-if="mediaTab === 'music'" class="tab-content">
          <div class="section-header">
            <h2>Music</h2>
            <button class="btn-primary" @click="showMusicForm = true; editingMusic = null">
              + 새 음악
            </button>
          </div>
          
          <div class="data-table">
            <table>
              <thead>
                <tr>
                  <th>제목</th>
                  <th>아티스트</th>
                  <th>커버 URL</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="music in musicList" :key="music.id">
                  <td>{{ music.title }}</td>
                  <td>{{ music.artist }}</td>
                  <td>{{ music.coverUrl }}</td>
                  <td>
                    <button class="btn-edit" @click="editMusic(music)">수정</button>
                    <button class="btn-delete" @click="deleteMusic(music.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Music 폼 -->
          <div v-if="showMusicForm" class="modal-overlay" @click.self="showMusicForm = false">
            <div class="modal-content">
              <h2>{{ editingMusic ? '음악 수정' : '새 음악' }}</h2>
              <form @submit.prevent="saveMusic">
                <div class="form-group">
                  <label>제목</label>
                  <input v-model="musicForm.title" required>
                </div>
                <div class="form-group">
                  <label>아티스트</label>
                  <input v-model="musicForm.artist" required>
                </div>
                <div class="form-group">
                  <label>커버 이미지 URL</label>
                  <input v-model="musicForm.coverUrl" required>
                </div>
                <div class="form-group">
                  <label>링크 URL</label>
                  <input v-model="musicForm.linkUrl">
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="musicForm.displayOrder">
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn-primary">저장</button>
                  <button type="button" class="btn-secondary" @click="showMusicForm = false">취소</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- Videos 탭 -->
        <div v-if="mediaTab === 'videos'" class="tab-content">
          <div class="section-header">
            <h2>Videos</h2>
            <button class="btn-primary" @click="showVideoForm = true; editingVideo = null">
              + 새 비디오
            </button>
          </div>
          
          <div class="data-table">
            <table>
              <thead>
                <tr>
                  <th>제목</th>
                  <th>Embed URL</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="video in videos" :key="video.id">
                  <td>{{ video.title }}</td>
                  <td>{{ video.embedUrl }}</td>
                  <td>
                    <button class="btn-edit" @click="editVideo(video)">수정</button>
                    <button class="btn-delete" @click="deleteVideo(video.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Video 폼 -->
          <div v-if="showVideoForm" class="modal-overlay" @click.self="showVideoForm = false">
            <div class="modal-content">
              <h2>{{ editingVideo ? '비디오 수정' : '새 비디오' }}</h2>
              <form @submit.prevent="saveVideo">
                <div class="form-group">
                  <label>제목</label>
                  <input v-model="videoForm.title" required>
                </div>
                <div class="form-group">
                  <label>YouTube Embed URL</label>
                  <input v-model="videoForm.embedUrl" required>
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="videoForm.displayOrder">
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn-primary">저장</button>
                  <button type="button" class="btn-secondary" @click="showVideoForm = false">취소</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- Photos 탭 -->
        <div v-if="mediaTab === 'photos'" class="tab-content">
          <div class="section-header">
            <h2>Photo Groups</h2>
            <button class="btn-primary" @click="showPhotoGroupForm = true; editingPhotoGroup = null">
              + 새 사진 그룹
            </button>
          </div>
          
          <div class="data-table">
            <table>
              <thead>
                <tr>
                  <th>제목</th>
                  <th>사진 수</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="group in photoGroups" :key="group.id">
                  <td>{{ group.title }}</td>
                  <td>{{ group.photos ? group.photos.length : 0 }}</td>
                  <td>
                    <button class="btn-edit" @click="editPhotoGroup(group)">수정</button>
                    <button class="btn-delete" @click="deletePhotoGroup(group.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- PhotoGroup 폼 -->
          <div v-if="showPhotoGroupForm" class="modal-overlay" @click.self="showPhotoGroupForm = false">
            <div class="modal-content">
              <h2>{{ editingPhotoGroup ? '사진 그룹 수정' : '새 사진 그룹' }}</h2>
              <form @submit.prevent="savePhotoGroup">
                <div class="form-group">
                  <label>제목</label>
                  <input v-model="photoGroupForm.title" required>
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="photoGroupForm.displayOrder">
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn-primary">저장</button>
                  <button type="button" class="btn-secondary" @click="showPhotoGroupForm = false">취소</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- News 탭 -->
        <div v-if="mediaTab === 'news'" class="tab-content">
          <div class="section-header">
            <h2>News</h2>
            <button class="btn-primary" @click="showNewsForm = true; editingNews = null">
              + 새 뉴스
            </button>
          </div>
          
          <div class="data-table">
            <table>
              <thead>
                <tr>
                  <th>날짜</th>
                  <th>제목</th>
                  <th>설명</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="news in newsList" :key="news.id">
                  <td>{{ news.date }}</td>
                  <td>{{ news.title }}</td>
                  <td>{{ news.description?.substring(0, 50) }}...</td>
                  <td>
                    <button class="btn-edit" @click="editNews(news)">수정</button>
                    <button class="btn-delete" @click="deleteNews(news.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- News 폼 -->
          <div v-if="showNewsForm" class="modal-overlay" @click.self="showNewsForm = false">
            <div class="modal-content">
              <h2>{{ editingNews ? '뉴스 수정' : '새 뉴스' }}</h2>
              <form @submit.prevent="saveNews">
                <div class="form-group">
                  <label>날짜</label>
                  <input type="date" v-model="newsForm.date" required>
                </div>
                <div class="form-group">
                  <label>제목</label>
                  <input v-model="newsForm.title" required>
                </div>
                <div class="form-group">
                  <label>설명</label>
                  <textarea v-model="newsForm.description" rows="4"></textarea>
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="newsForm.displayOrder">
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn-primary">저장</button>
                  <button type="button" class="btn-secondary" @click="showNewsForm = false">취소</button>
                </div>
              </form>
            </div>
          </div>
        </div>

        <!-- Equipment 탭 -->
        <div v-if="mediaTab === 'equipment'" class="tab-content">
          <div class="section-header">
            <h2>Equipment</h2>
            <button class="btn-primary" @click="showEquipmentForm = true; editingEquipment = null">
              + 새 장비
            </button>
          </div>
          
          <div class="data-table">
            <table>
              <thead>
                <tr>
                  <th>이름</th>
                  <th>이미지 URL</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="equipment in equipmentList" :key="equipment.id">
                  <td>{{ equipment.name }}</td>
                  <td>{{ equipment.imageUrl }}</td>
                  <td>
                    <button class="btn-edit" @click="editEquipment(equipment)">수정</button>
                    <button class="btn-delete" @click="deleteEquipment(equipment.id)">삭제</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Equipment 폼 -->
          <div v-if="showEquipmentForm" class="modal-overlay" @click.self="showEquipmentForm = false">
            <div class="modal-content">
              <h2>{{ editingEquipment ? '장비 수정' : '새 장비' }}</h2>
              <form @submit.prevent="saveEquipment">
                <div class="form-group">
                  <label>이름</label>
                  <input v-model="equipmentForm.name" required>
                </div>
                <div class="form-group">
                  <label>이미지 URL</label>
                  <input v-model="equipmentForm.imageUrl">
                </div>
                <div class="form-group">
                  <label>표시 순서</label>
                  <input type="number" v-model="equipmentForm.displayOrder">
                </div>
                <div class="form-actions">
                  <button type="submit" class="btn-primary">저장</button>
                  <button type="button" class="btn-secondary" @click="showEquipmentForm = false">취소</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- Contact 관리 -->
      <div v-if="activeSection === 'contact'" class="content-section">
        <div class="section-header">
          <h1 class="section-title">Contact 관리</h1>
          <button class="btn-primary" @click="showContactForm = true; editingContact = null">
            + 새 연락처
          </button>
        </div>
        
        <div class="data-table">
          <table>
            <thead>
              <tr>
                <th>이름</th>
                <th>역할</th>
                <th>이메일</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="contact in contactList" :key="contact.id">
                <td>{{ contact.name }}</td>
                <td>{{ contact.role }}</td>
                <td>{{ contact.email }}</td>
                <td>
                  <button class="btn-edit" @click="editContact(contact)">수정</button>
                  <button class="btn-delete" @click="deleteContact(contact.id)">삭제</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Contact 폼 -->
        <div v-if="showContactForm" class="modal-overlay" @click.self="showContactForm = false">
          <div class="modal-content">
            <h2>{{ editingContact ? '연락처 수정' : '새 연락처' }}</h2>
            <form @submit.prevent="saveContact">
              <div class="form-group">
                <label>이름</label>
                <input v-model="contactForm.name" required>
              </div>
              <div class="form-group">
                <label>역할</label>
                <input v-model="contactForm.role">
              </div>
              <div class="form-group">
                <label>이메일</label>
                <input type="email" v-model="contactForm.email" required>
              </div>
              <div class="form-group">
                <label>안내 문구</label>
                <textarea v-model="contactForm.introText" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>Portfolio EN URL</label>
                <input v-model="contactForm.portfolioEnUrl">
              </div>
              <div class="form-group">
                <label>Portfolio DE URL</label>
                <input v-model="contactForm.portfolioDeUrl">
              </div>
              <div class="form-group">
                <label>Portfolio KR URL</label>
                <input v-model="contactForm.portfolioKrUrl">
              </div>
              <div class="form-group">
                <label>Stage Rider URL</label>
                <input v-model="contactForm.stageRiderUrl">
              </div>
              <div class="form-group">
                <label>CV URL</label>
                <input v-model="contactForm.cvUrl">
              </div>
              <div class="form-group">
                <label>표시 순서</label>
                <input type="number" v-model="contactForm.displayOrder">
              </div>
              <div class="form-actions">
                <button type="submit" class="btn-primary">저장</button>
                <button type="button" class="btn-secondary" @click="showContactForm = false">취소</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../api/axios'

// 활성 섹션
const activeSection = ref('dashboard')
const mediaTab = ref('music')

// Projects
const projects = ref([])
const showProjectForm = ref(false)
const editingProject = ref(null)
const projectForm = ref({
  title: '',
  subtitle: '',
  premiereDate: '',
  location: '',
  description1: '',
  description2: '',
  description3: '',
  mainImageUrl: '',
  horizontal1ImageUrl: '',
  horizontal2ImageUrl: '',
  vertical1ImageUrl: '',
  vertical2ImageUrl: '',
  coverImageUrl: '',
  moreInfoUrl: '',
  review1Text: '',
  review1Source: '',
  review2Text: '',
  review2Source: '',
  urlSlug: '',
  displayOrder: 0
})

// Concerts
const concerts = ref([])
const showConcertForm = ref(false)
const editingConcert = ref(null)
const concertForm = ref({
  date: '',
  location: '',
  name: '',
  startTime: '',
  ticketInfo: '',
  fullLocation: '',
  googleMapUrl: '',
  collaborationInfo: '',
  isPast: false
})

// Music
const musicList = ref([])
const showMusicForm = ref(false)
const editingMusic = ref(null)
const musicForm = ref({
  title: '',
  artist: '',
  coverUrl: '',
  linkUrl: '',
  displayOrder: 0
})

// Videos
const videos = ref([])
const showVideoForm = ref(false)
const editingVideo = ref(null)
const videoForm = ref({
  title: '',
  embedUrl: '',
  displayOrder: 0
})

// PhotoGroups
const photoGroups = ref([])
const showPhotoGroupForm = ref(false)
const editingPhotoGroup = ref(null)
const photoGroupForm = ref({
  title: '',
  displayOrder: 0
})

// News
const newsList = ref([])
const showNewsForm = ref(false)
const editingNews = ref(null)
const newsForm = ref({
  date: '',
  title: '',
  description: '',
  displayOrder: 0
})

// Equipment
const equipmentList = ref([])
const showEquipmentForm = ref(false)
const editingEquipment = ref(null)
const equipmentForm = ref({
  name: '',
  imageUrl: '',
  displayOrder: 0
})

// Contact
const contactList = ref([])
const showContactForm = ref(false)
const editingContact = ref(null)
const contactForm = ref({
  name: '',
  role: '',
  email: '',
  introText: '',
  portfolioEnUrl: '',
  portfolioDeUrl: '',
  portfolioKrUrl: '',
  stageRiderUrl: '',
  cvUrl: '',
  displayOrder: 0
})

// API 호출 함수들
const loadProjects = async () => {
  try {
    const response = await axios.get('/api/projects')
    projects.value = response.data
  } catch (error) {
    console.error('프로젝트 로드 실패:', error)
  }
}

const editProject = (project) => {
  editingProject.value = project
  projectForm.value = { ...project }
  showProjectForm.value = true
}

const saveProject = async () => {
  try {
    if (editingProject.value) {
      await axios.put(`/api/projects/${editingProject.value.id}`, projectForm.value)
    } else {
      await axios.post('/api/projects', projectForm.value)
    }
    showProjectForm.value = false
    loadProjects()
  } catch (error) {
    console.error('프로젝트 저장 실패:', error)
  }
}

const deleteProject = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/projects/${id}`)
      loadProjects()
    } catch (error) {
      console.error('프로젝트 삭제 실패:', error)
    }
  }
}

const loadConcerts = async () => {
  try {
    const response = await axios.get('/api/concerts')
    concerts.value = response.data
  } catch (error) {
    console.error('콘서트 로드 실패:', error)
  }
}

const editConcert = (concert) => {
  editingConcert.value = concert
  concertForm.value = { ...concert }
  showConcertForm.value = true
}

const saveConcert = async () => {
  try {
    if (editingConcert.value) {
      await axios.put(`/api/concerts/${editingConcert.value.id}`, concertForm.value)
    } else {
      await axios.post('/api/concerts', concertForm.value)
    }
    showConcertForm.value = false
    loadConcerts()
  } catch (error) {
    console.error('콘서트 저장 실패:', error)
  }
}

const deleteConcert = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/concerts/${id}`)
      loadConcerts()
    } catch (error) {
      console.error('콘서트 삭제 실패:', error)
    }
  }
}

const loadMusic = async () => {
  try {
    const response = await axios.get('/api/media/music')
    musicList.value = response.data
  } catch (error) {
    console.error('음악 로드 실패:', error)
  }
}

const editMusic = (music) => {
  editingMusic.value = music
  musicForm.value = { ...music }
  showMusicForm.value = true
}

const saveMusic = async () => {
  try {
    if (editingMusic.value) {
      await axios.put(`/api/media/music/${editingMusic.value.id}`, musicForm.value)
    } else {
      await axios.post('/api/media/music', musicForm.value)
    }
    showMusicForm.value = false
    loadMusic()
  } catch (error) {
    console.error('음악 저장 실패:', error)
  }
}

const deleteMusic = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/music/${id}`)
      loadMusic()
    } catch (error) {
      console.error('음악 삭제 실패:', error)
    }
  }
}

const loadVideos = async () => {
  try {
    const response = await axios.get('/api/media/videos')
    videos.value = response.data
  } catch (error) {
    console.error('비디오 로드 실패:', error)
  }
}

const editVideo = (video) => {
  editingVideo.value = video
  videoForm.value = { ...video }
  showVideoForm.value = true
}

const saveVideo = async () => {
  try {
    if (editingVideo.value) {
      await axios.put(`/api/media/videos/${editingVideo.value.id}`, videoForm.value)
    } else {
      await axios.post('/api/media/videos', videoForm.value)
    }
    showVideoForm.value = false
    loadVideos()
  } catch (error) {
    console.error('비디오 저장 실패:', error)
  }
}

const deleteVideo = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/videos/${id}`)
      loadVideos()
    } catch (error) {
      console.error('비디오 삭제 실패:', error)
    }
  }
}

const loadPhotoGroups = async () => {
  try {
    const response = await axios.get('/api/media/photo-groups')
    photoGroups.value = response.data
  } catch (error) {
    console.error('사진 그룹 로드 실패:', error)
  }
}

const editPhotoGroup = (group) => {
  editingPhotoGroup.value = group
  photoGroupForm.value = { title: group.title, displayOrder: group.displayOrder }
  showPhotoGroupForm.value = true
}

const savePhotoGroup = async () => {
  try {
    if (editingPhotoGroup.value) {
      await axios.put(`/api/media/photo-groups/${editingPhotoGroup.value.id}`, photoGroupForm.value)
    } else {
      await axios.post('/api/media/photo-groups', photoGroupForm.value)
    }
    showPhotoGroupForm.value = false
    loadPhotoGroups()
  } catch (error) {
    console.error('사진 그룹 저장 실패:', error)
  }
}

const deletePhotoGroup = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/photo-groups/${id}`)
      loadPhotoGroups()
    } catch (error) {
      console.error('사진 그룹 삭제 실패:', error)
    }
  }
}

const loadNews = async () => {
  try {
    const response = await axios.get('/api/media/news')
    newsList.value = response.data
  } catch (error) {
    console.error('뉴스 로드 실패:', error)
  }
}

const editNews = (news) => {
  editingNews.value = news
  newsForm.value = { ...news }
  showNewsForm.value = true
}

const saveNews = async () => {
  try {
    if (editingNews.value) {
      await axios.put(`/api/media/news/${editingNews.value.id}`, newsForm.value)
    } else {
      await axios.post('/api/media/news', newsForm.value)
    }
    showNewsForm.value = false
    loadNews()
  } catch (error) {
    console.error('뉴스 저장 실패:', error)
  }
}

const deleteNews = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/news/${id}`)
      loadNews()
    } catch (error) {
      console.error('뉴스 삭제 실패:', error)
    }
  }
}

const loadEquipment = async () => {
  try {
    const response = await axios.get('/api/media/equipment')
    equipmentList.value = response.data
  } catch (error) {
    console.error('장비 로드 실패:', error)
  }
}

const editEquipment = (equipment) => {
  editingEquipment.value = equipment
  equipmentForm.value = { ...equipment }
  showEquipmentForm.value = true
}

const saveEquipment = async () => {
  try {
    if (editingEquipment.value) {
      await axios.put(`/api/media/equipment/${editingEquipment.value.id}`, equipmentForm.value)
    } else {
      await axios.post('/api/media/equipment', equipmentForm.value)
    }
    showEquipmentForm.value = false
    loadEquipment()
  } catch (error) {
    console.error('장비 저장 실패:', error)
  }
}

const deleteEquipment = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/equipment/${id}`)
      loadEquipment()
    } catch (error) {
      console.error('장비 삭제 실패:', error)
    }
  }
}

const loadContact = async () => {
  try {
    const response = await axios.get('/api/contact')
    contactList.value = response.data
  } catch (error) {
    console.error('연락처 로드 실패:', error)
  }
}

const editContact = (contact) => {
  editingContact.value = contact
  contactForm.value = { ...contact }
  showContactForm.value = true
}

const saveContact = async () => {
  try {
    if (editingContact.value) {
      await axios.put(`/api/contact/${editingContact.value.id}`, contactForm.value)
    } else {
      await axios.post('/api/contact', contactForm.value)
    }
    showContactForm.value = false
    loadContact()
  } catch (error) {
    console.error('연락처 저장 실패:', error)
  }
}

const deleteContact = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/contact/${id}`)
      loadContact()
    } catch (error) {
      console.error('연락처 삭제 실패:', error)
    }
  }
}

onMounted(() => {
  loadProjects()
  loadConcerts()
  loadMusic()
  loadVideos()
  loadPhotoGroups()
  loadNews()
  loadEquipment()
  loadContact()
})
</script>

<style scoped>
.admin {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
  overflow: hidden;
}

/* 사이드바 */
.admin-sidebar {
  background: #2c3e50;
  color: white;
  padding: 2rem;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 2rem;
  color: #ecf0f1;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  color: #bdc3c7;
  text-align: left;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
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

/* 메인 콘텐츠 */
.admin-content {
  flex: 1;
  padding: 2rem;
  height: 100vh;
  overflow-y: auto;
}

.content-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.section-title {
  font-size: 2rem;
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

/* Dashboard */
.dashboard-card {
  background: #ecf0f1;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.dashboard-card h3 {
  margin-bottom: 1rem;
  color: #2c3e50;
}

.stats-placeholder {
  background: white;
  padding: 2rem;
  border-radius: 4px;
  text-align: center;
  color: #7f8c8d;
}

/* 탭 */
.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 2px solid #ecf0f1;
}

.tab {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: none;
  color: #7f8c8d;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
  font-size: 1rem;
}

.tab:hover {
  color: #2c3e50;
}

.tab.active {
  color: #3498db;
  border-bottom-color: #3498db;
}

.tab-content {
  margin-top: 2rem;
}

/* 테이블 */
.data-table {
  overflow-x: auto;
  margin-top: 1rem;
}

table {
  border-collapse: collapse;
}

thead {
  background: #ecf0f1;
}

th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
}

td {
  padding: 1rem;
  border-bottom: 1px solid #ecf0f1;
}

tbody tr:hover {
  background: #f8f9fa;
}

/* 버튼 */
.btn-primary {
  padding: 0.75rem 1.5rem;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.btn-secondary:hover {
  background: #7f8c8d;
}

.btn-edit {
  padding: 0.5rem 1rem;
  background: #f39c12;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 0.5rem;
  transition: background 0.3s;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-delete {
  padding: 0.5rem 1rem;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-delete:hover {
  background: #c0392b;
}

/* 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  overflow-y: auto;
  padding: 2rem;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-bottom: 1.5rem;
  color: #2c3e50;
}

/* 폼 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2c3e50;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input[type="checkbox"] {
  margin-right: 0.5rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}
</style>
