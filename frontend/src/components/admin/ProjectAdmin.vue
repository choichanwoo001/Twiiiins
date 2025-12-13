<template>
  <div class="content-section">
    <h1 class="section-title">Projects 관리</h1>
    
    <!-- 검색/필터 섹션 -->
    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label>제목</label>
          <input v-model="searchFilters.title" placeholder="제목을 입력하세요" />
        </div>
        <div class="filter-group">
          <label>장소</label>
          <input v-model="searchFilters.location" placeholder="장소를 입력하세요" />
        </div>
        <div class="filter-group">
          <label>날짜 범위</label>
          <div class="date-range">
            <input type="date" v-model="searchFilters.startDate" />
            <span>~</span>
            <input type="date" v-model="searchFilters.endDate" />
          </div>
        </div>
        <div class="filter-actions">
          <BaseButton variant="secondary" @click="resetFilters">초기화</BaseButton>
          <BaseButton variant="primary" @click="searchProjects">검색</BaseButton>
        </div>
      </div>
    </div>

    <!-- 프로젝트 목록 -->
    <div class="projects-list">
      <h2 class="projects-header">전체 목록</h2>
      <div class="projects-table">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>제목</th>
              <th>부제목</th>
              <th>장소</th>
              <th>일시</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(project, index) in displayedProjects" :key="project.id">
              <td>{{ index + 1 }}</td>
              <td>{{ project.title }}</td>
              <td>{{ project.subtitle || '-' }}</td>
              <td>{{ project.location || '-' }}</td>
              <td>{{ formatDate(project.premiereDate) }}</td>
              <td>
                <div class="action-buttons">
                  <BaseButton size="small" variant="secondary" @click="editProject(project)">수정</BaseButton>
                  <BaseButton size="small" variant="info" @click="editProjectDetail(project)">상세 등록/수정</BaseButton>
                  <BaseButton size="small" variant="danger" @click="deleteProject(project.id)">삭제</BaseButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 프로젝트 등록/수정 폼 (Projects 페이지용) -->
    <div class="project-form-section" v-if="!showProjectDetailForm">
      <h2>{{ editingProject ? '프로젝트 수정' : '새 프로젝트 등록' }}</h2>
      <form @submit.prevent="saveProject" class="project-form">
        <div class="form-group">
          <label>제목 *</label>
          <input v-model="form.title" required />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>일시 (Premiere Date) *</label>
            <input type="date" v-model="form.premiereDate" required />
          </div>
          <div class="form-group">
            <label>장소 *</label>
            <input v-model="form.location" required />
          </div>
        </div>

        <div class="form-group">
          <label>대표 사진 *</label>
          <input type="file" @change="handleCoverImageChange" accept="image/*" />
          <div v-if="form.coverImageUrl" class="image-preview">
            <img :src="form.coverImageUrl" alt="대표 사진 미리보기" />
            <button type="button" @click="form.coverImageUrl = null">제거</button>
          </div>
          <div v-else-if="uploadedCoverImageUrl" class="image-preview">
            <img :src="uploadedCoverImageUrl" alt="대표 사진 미리보기" />
          </div>
        </div>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary">{{ editingProject ? '수정' : '등록' }}</BaseButton>
          <BaseButton type="button" variant="secondary" @click="cancelEdit" v-if="editingProject">취소</BaseButton>
        </div>
      </form>
    </div>

    <!-- 프로젝트 상세 등록/수정 폼 (ProjectDetail 페이지용) -->
    <div class="project-detail-form-section" v-if="showProjectDetailForm">
      <div class="form-header">
        <h2>{{ editingProjectDetail ? '프로젝트 상세 수정' : '프로젝트 상세 등록' }}</h2>
        <BaseButton variant="secondary" @click="showProjectDetailForm = false; cancelEditDetail()">목록 폼으로</BaseButton>
      </div>
      <form @submit.prevent="saveProjectDetail" class="project-detail-form">
        <!-- 기본 정보 -->
        <div class="form-section">
          <h3>기본 정보</h3>
          <div class="form-group">
            <label>제목 *</label>
            <input v-model="detailForm.title" required />
          </div>
          <div class="form-group">
            <label>부제목</label>
            <input v-model="detailForm.subtitle" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>일시 (Premiere Date) *</label>
              <input type="date" v-model="detailForm.premiereDate" required />
            </div>
            <div class="form-group">
              <label>장소</label>
              <input v-model="detailForm.location" />
            </div>
          </div>
          <div class="form-group">
            <label>감독</label>
            <input v-model="detailForm.director" />
          </div>
          <div class="form-group">
            <label>프로젝트 상세 페이지 링크</label>
            <div class="url-slug-display">
              <input 
                v-model="detailForm.urlSlug" 
                readonly 
                :placeholder="generateUrlSlug()"
                class="readonly-input"
              />
              <a 
                v-if="detailForm.urlSlug" 
                :href="projectDetailUrl" 
                target="_blank" 
                class="link-button"
              >
                링크 열기 ↗
              </a>
            </div>
            <small class="form-hint">제목에서 자동으로 생성됩니다. 프로젝트 상세 페이지는 <code>/projects/{{ detailForm.urlSlug || generateUrlSlug() }}</code> 경로에서 접근할 수 있습니다.</small>
          </div>
        </div>

        <!-- 설명 -->
        <div class="form-section">
          <h3>설명</h3>
          <div class="descriptions-container">
            <div v-for="(description, index) in detailForm.descriptions" :key="index" class="description-item-form">
              <div class="description-item-header">
                <label>설명 {{ index + 1 }}</label>
                <BaseButton 
                  type="button" 
                  variant="danger" 
                  size="small" 
                  @click="removeDescription(index)"
                >
                  삭제
                </BaseButton>
              </div>
              <div class="form-group">
                <textarea v-model="detailForm.descriptions[index]" rows="4"></textarea>
              </div>
            </div>
            <div class="form-group">
              <BaseButton type="button" variant="secondary" @click="addDescription">
                설명 추가
              </BaseButton>
            </div>
          </div>
          <div class="form-group">
            <label>감사 문구</label>
            <textarea v-model="detailForm.thankYouText" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>추가 정보 URL</label>
            <input v-model="detailForm.moreInfoUrl" type="url" />
          </div>
        </div>

        <!-- 이미지 -->
        <div class="form-section">
          <h3>이미지</h3>
          <div class="form-group">
            <label>사진 관리</label>
            <BaseButton variant="info" @click="manageProjectImages(editingProjectDetail || detailForm)">
              사진 관리
            </BaseButton>
            <div v-if="detailForm.imageUrls && detailForm.imageUrls.length > 0" class="image-count">
              등록된 사진: {{ detailForm.imageUrls.length }}개
            </div>
            <p class="form-help-text">목록 페이지에 표시되는 대표 이미지는 기본 등록에서 설정한 커버 이미지가 사용됩니다.</p>
          </div>
        </div>

        <!-- 리뷰 -->
        <div class="form-section">
          <h3>리뷰</h3>
          <div class="reviews-container">
            <div v-for="(review, index) in detailForm.reviews" :key="index" class="review-item-form">
              <div class="review-item-header">
                <label>리뷰 {{ index + 1 }}</label>
                <BaseButton 
                  type="button" 
                  variant="danger" 
                  size="small" 
                  @click="removeReview(index)"
                >
                  삭제
                </BaseButton>
              </div>
              <div class="form-group">
                <label>리뷰 텍스트</label>
                <textarea v-model="review.text" rows="3"></textarea>
              </div>
              <div class="form-group">
                <label>리뷰 출처</label>
                <input v-model="review.source" />
              </div>
            </div>
            <div class="form-group">
              <BaseButton type="button" variant="secondary" @click="addReview">
                리뷰 추가
              </BaseButton>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary">{{ editingProjectDetail ? '수정' : '등록' }}</BaseButton>
          <BaseButton type="button" variant="secondary" @click="cancelEditDetail" v-if="editingProjectDetail">취소</BaseButton>
        </div>
      </form>
    </div>

    <!-- 공통 다이얼로그 -->
    <ConfirmDialog
      :is-visible="confirmDialog.isVisible"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :confirm-text="confirmDialog.confirmText"
      :cancel-text="confirmDialog.cancelText"
      :confirm-variant="confirmDialog.confirmVariant"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />

    <AlertDialog
      :is-visible="alertDialog.isVisible"
      :title="alertDialog.title"
      :message="alertDialog.message"
      :button-text="alertDialog.buttonText"
      :button-variant="alertDialog.buttonVariant"
      @close="handleAlertClose"
    />

    <!-- 사진 관리 모달 -->
    <Modal
      :is-visible="!!selectedProject"
      :title="selectedProject ? `${selectedProject.title} - 사진 관리` : ''"
      @close="closeImageModal"
    >
      <div v-if="selectedProject">
        <!-- 사진 목록 -->
        <div class="photos-grid" v-if="selectedProject.imageUrls && selectedProject.imageUrls.length > 0">
          <div v-for="(imageUrl, index) in selectedProject.imageUrls" :key="index" class="photo-item">
            <div class="photo-image-wrapper">
              <img :src="getImageUrl(imageUrl)" :alt="`Photo ${index + 1}`" class="photo-image" />
            </div>
            <div class="photo-actions">
              <button class="btn-delete-small" @click="deleteProjectPhoto(index)">삭제</button>
            </div>
          </div>
        </div>
        <div v-else class="no-photos">
          등록된 사진이 없습니다.
        </div>

        <!-- 사진 업로드 -->
        <div class="photo-upload">
          <h4>사진 추가</h4>
          <input type="file" ref="projectFileInput" multiple @change="handleProjectFileSelect" accept="image/*" />
          <button class="btn-upload" @click="uploadProjectPhotos">업로드</button>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from '../../api/axios'
import { BaseButton, ConfirmDialog, AlertDialog } from '../common'
import Modal from './common/Modal.vue'
import { formatDate } from '../../utils/commonHelpers'
import { logError, getErrorMessage } from '../../utils/errorHandler'
import {
  buildProjectCreatePayload,
  buildProjectUpdatePayload,
  buildProjectDetailPayload,
  sanitizeQueryParams
} from '../../services/payloadMappers'

// Reactive data
const projects = ref([])
const filteredProjects = ref([])
const isFiltered = ref(false)
const searchFilters = ref({
  title: '',
  location: '',
  startDate: '',
  endDate: ''
})
const form = ref({
  title: '',
  premiereDate: '',
  location: '',
  coverImageUrl: null
})
const detailForm = ref({
  title: '',
  subtitle: '',
  premiereDate: '',
  location: '',
  director: '',
  urlSlug: '',
  descriptions: [],
  thankYouText: '',
  moreInfoUrl: '',
  coverImageUrl: null,
  imageUrls: [],
  reviews: []
})
const editingProject = ref(null)
const editingProjectDetail = ref(null)
const showProjectDetailForm = ref(false)
const uploadedCoverImageUrl = ref(null)
const selectedProject = ref(null)
const projectFileInput = ref(null)
const selectedProjectFiles = ref([])

const displayedProjects = computed(() =>
  isFiltered.value ? filteredProjects.value : projects.value
)

// 다이얼로그 상태
const confirmDialog = ref({
  isVisible: false,
  title: '확인',
  message: '',
  confirmText: '확인',
  cancelText: '취소',
  confirmVariant: 'danger',
  resolve: null
})

const alertDialog = ref({
  isVisible: false,
  title: '알림',
  message: '',
  buttonText: '확인',
  buttonVariant: 'primary',
  resolve: null
})

// 다이얼로그 헬퍼 함수
const showConfirm = (message, title = '확인') => {
  return new Promise((resolve) => {
    confirmDialog.value = {
      isVisible: true,
      title,
      message,
      confirmText: '확인',
      cancelText: '취소',
      confirmVariant: 'danger',
      resolve
    }
  })
}

const showAlert = (message, title = '알림', variant = 'primary') => {
  return new Promise((resolve) => {
    alertDialog.value = {
      isVisible: true,
      title,
      message,
      buttonText: '확인',
      buttonVariant: variant,
      resolve
    }
  })
}

const handleConfirm = () => {
  if (confirmDialog.value.resolve) {
    confirmDialog.value.resolve(true)
  }
  confirmDialog.value.isVisible = false
}

const handleCancel = () => {
  if (confirmDialog.value.resolve) {
    confirmDialog.value.resolve(false)
  }
  confirmDialog.value.isVisible = false
}

const handleAlertClose = () => {
  if (alertDialog.value.resolve) {
    alertDialog.value.resolve()
  }
  alertDialog.value.isVisible = false
}

// 이미지 업로드
const uploadImage = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await axios.post('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    // FileUploadResponseDto는 직접 반환되므로 response.data.url 사용
    const url = response.data.url || response.data.data?.url
    if (!url) {
      console.error('이미지 업로드 응답에 URL이 없습니다:', response.data)
      throw new Error('이미지 업로드 응답에 URL이 없습니다')
    }
    return url
  } catch (error) {
    logError(error, '이미지 업로드')
    throw error
  }
}

const handleCoverImageChange = async (event) => {
  const file = event.target.files[0]
  if (file) {
    try {
      const url = await uploadImage(file)
      uploadedCoverImageUrl.value = url
      form.value.coverImageUrl = url
    } catch (error) {
      await showAlert(getErrorMessage(error), '오류', 'danger')
    }
  }
}


// Methods
const loadProjects = async () => {
  try {
    const response = await axios.get('/projects')
    projects.value = response.data.data || response.data || []
    if (isFiltered.value) {
      await searchProjects()
    } else {
      filteredProjects.value = []
    }
  } catch (error) {
    logError(error, '프로젝트 로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const searchProjects = async () => {
  try {
    const params = sanitizeQueryParams(searchFilters.value)
    const response = await axios.get('/projects', { params })
    filteredProjects.value = response.data.data || response.data || []
    isFiltered.value = true
  } catch (error) {
    logError(error, '프로젝트 검색')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const resetFilters = () => {
  searchFilters.value = {
    title: '',
    location: '',
    startDate: '',
    endDate: ''
  }
  isFiltered.value = false
  filteredProjects.value = []
  loadProjects()
}

const editProject = (project) => {
  editingProject.value = project
  showProjectDetailForm.value = false
  form.value = {
    title: project.title || '',
    premiereDate: project.premiereDate || '',
    location: project.location || '',
    coverImageUrl: project.coverImageUrl || null
  }
  uploadedCoverImageUrl.value = project.coverImageUrl || null
}

const slugify = (value) => {
  const title = (value || '').toString().trim().toLowerCase()
  if (!title) return ''
  return title
    .replace(/[^\w\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-+|-+$/g, '')
}

// URL Slug 자동 생성 함수
const generateUrlSlug = () => slugify(detailForm.value.title)

// 프로젝트 상세 페이지 링크
const projectDetailUrl = computed(() => {
  const slug = detailForm.value.urlSlug || generateUrlSlug()
  if (!slug) return ''
  const baseUrl = window.location.origin
  return `${baseUrl}/projects/${slug}`
})

// 제목 변경 시 URL Slug 자동 생성
watch(() => detailForm.value.title, (newTitle) => {
  if (newTitle && !editingProjectDetail.value?.urlSlug) {
    // 새로 생성 중이거나 urlSlug가 없을 때만 자동 생성
    detailForm.value.urlSlug = generateUrlSlug()
  } else if (newTitle && editingProjectDetail.value?.urlSlug) {
    // 기존 프로젝트의 경우 제목 변경 시 새 slug 생성 (수동으로 변경 가능하도록)
    // 하지만 자동 생성은 하지 않고, 사용자가 원하면 수동으로 변경 가능
  }
})

const editProjectDetail = (project) => {
  editingProjectDetail.value = project
  showProjectDetailForm.value = true
  const urlSlug = project.urlSlug || slugify(project.title)
  
  // 리뷰 데이터 변환
  let reviews = []
  if (project.reviews && Array.isArray(project.reviews) && project.reviews.length > 0) {
    reviews = project.reviews.map(r => ({ text: r.text || '', source: r.source || '' }))
  }
  
  // 설명 데이터 변환
  let descriptions = []
  if (project.descriptions && Array.isArray(project.descriptions)) {
    descriptions = project.descriptions
  }
  
  detailForm.value = {
    title: project.title || '',
    subtitle: project.subtitle || '',
    premiereDate: project.premiereDate || '',
    location: project.location || '',
    director: project.director || '',
    urlSlug: urlSlug,
    descriptions: descriptions,
    thankYouText: project.thankYouText || '',
    moreInfoUrl: project.moreInfoUrl || '',
    coverImageUrl: project.coverImageUrl || null,
    imageUrls: project.imageUrls || [],
    reviews: reviews
  }
}

const cancelEdit = () => {
  editingProject.value = null
  form.value = {
    title: '',
    premiereDate: '',
    location: '',
    coverImageUrl: null
  }
  uploadedCoverImageUrl.value = null
}

const cancelEditDetail = () => {
  editingProjectDetail.value = null
  detailForm.value = {
    title: '',
    subtitle: '',
    premiereDate: '',
    location: '',
    director: '',
    urlSlug: '',
    descriptions: [],
    thankYouText: '',
    moreInfoUrl: '',
    coverImageUrl: null,
    imageUrls: [],
    reviews: []
  }
}

const saveProject = async () => {
  try {
    const coverImageUrl = form.value.coverImageUrl || uploadedCoverImageUrl.value || undefined
    const basePayload = {
      title: form.value.title,
      premiereDate: form.value.premiereDate,
      location: form.value.location,
      coverImageUrl,
      urlSlug: editingProject.value?.urlSlug || slugify(form.value.title)
    }

    let savedProject
    if (editingProject.value) {
      const payload = buildProjectUpdatePayload(basePayload)
      const response = await axios.put(`/projects/${editingProject.value.id}`, payload)
      savedProject = response.data.data || response.data
      await showAlert('프로젝트가 수정되었습니다.', '성공', 'success')
    } else {
      const payload = buildProjectCreatePayload(basePayload)
      const response = await axios.post('/projects', payload)
      savedProject = response.data.data || response.data
      await showAlert('프로젝트가 등록되었습니다. 상세 정보를 등록하시겠습니까?', '성공', 'success')
      
      // 새로 등록한 프로젝트를 선택하고 상세 등록 폼으로 전환
      if (savedProject && savedProject.id) {
        await loadProjects()
        const newProject = projects.value.find(p => p.id === savedProject.id)
        if (newProject) {
          editProjectDetail(newProject)
        }
      }
    }
    
    cancelEdit()
    await loadProjects()
  } catch (error) {
    logError(error, '프로젝트 저장')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

// 리뷰 추가/삭제 함수
const addReview = () => {
  if (!detailForm.value.reviews) {
    detailForm.value.reviews = []
  }
  detailForm.value.reviews.push({ text: '', source: '' })
}

const removeReview = (index) => {
  if (detailForm.value.reviews && detailForm.value.reviews.length > index) {
    detailForm.value.reviews.splice(index, 1)
  }
}

// 설명 추가/삭제 함수
const addDescription = () => {
  if (!detailForm.value.descriptions) {
    detailForm.value.descriptions = []
  }
  detailForm.value.descriptions.push('')
}

const removeDescription = (index) => {
  if (detailForm.value.descriptions && detailForm.value.descriptions.length > index) {
    detailForm.value.descriptions.splice(index, 1)
  }
}

const saveProjectDetail = async () => {
  try {
    // URL Slug가 없으면 제목에서 자동 생성
    const urlSlug = detailForm.value.urlSlug || generateUrlSlug()
    
    const basePayload = {
      title: detailForm.value.title,
      subtitle: detailForm.value.subtitle,
      premiereDate: detailForm.value.premiereDate,
      location: detailForm.value.location,
      director: detailForm.value.director,
      urlSlug,
      descriptions: detailForm.value.descriptions || [],
      thankYouText: detailForm.value.thankYouText,
      moreInfoUrl: detailForm.value.moreInfoUrl,
      coverImageUrl: detailForm.value.coverImageUrl || undefined,
      imageUrls: detailForm.value.imageUrls || [],
      reviews: detailForm.value.reviews || []
    }

    if (editingProjectDetail.value) {
      const payload = buildProjectUpdatePayload(basePayload)
      await axios.put(`/projects/${editingProjectDetail.value.id}`, payload)
      await showAlert('프로젝트 상세가 수정되었습니다.', '성공', 'success')
    } else {
      const payload = buildProjectDetailPayload(basePayload)
      await axios.post('/projects', payload)
      await showAlert('프로젝트 상세가 등록되었습니다.', '성공', 'success')
    }
    
    cancelEditDetail()
    showProjectDetailForm.value = false
    await loadProjects()
  } catch (error) {
    logError(error, '프로젝트 상세 저장')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

// 프로젝트 이미지 관리
const manageProjectImages = (project) => {
  selectedProject.value = project
  selectedProjectFiles.value = []
  if (!selectedProject.value.imageUrls) {
    selectedProject.value.imageUrls = []
  }
}

const closeImageModal = () => {
  selectedProject.value = null
  selectedProjectFiles.value = []
  if (projectFileInput.value) {
    projectFileInput.value.value = ''
  }
}

const handleProjectFileSelect = (event) => {
  selectedProjectFiles.value = Array.from(event.target.files)
}

const uploadProjectPhotos = async () => {
  if (!selectedProjectFiles.value.length || !selectedProject.value) {
    await showAlert('파일을 선택해주세요.', '알림', 'warning')
    return
  }

  try {
    const formData = new FormData()
    selectedProjectFiles.value.forEach(file => {
      formData.append('files', file)
    })

    const response = await axios.post('/upload/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    const uploadedUrls = response.data.data?.urls || response.data.urls || []
    
    if (selectedProject.value.id) {
      // 기존 프로젝트 업데이트
      const payload = buildProjectUpdatePayload({
        ...selectedProject.value,
        imageUrls: [...(selectedProject.value.imageUrls || []), ...uploadedUrls]
      })
      const updatedProjectResponse = await axios.put(`/projects/${selectedProject.value.id}`, payload)
      const updatedProject = updatedProjectResponse.data.data || updatedProjectResponse.data
      
      // 프로젝트 목록 새로고침
      await loadProjects()
      
      // 업데이트된 프로젝트 찾아서 selectedProject 업데이트
      const latestProject = projects.value.find(p => p.id === selectedProject.value.id)
      if (latestProject) {
        selectedProject.value = latestProject
        detailForm.value.imageUrls = latestProject.imageUrls || []
      } else {
        // 목록에서 찾을 수 없으면 서버 응답 사용
        selectedProject.value = updatedProject
        detailForm.value.imageUrls = updatedProject.imageUrls || []
      }
    } else {
      // 새 프로젝트 (아직 저장 전)
      if (!selectedProject.value.imageUrls) {
        selectedProject.value.imageUrls = []
      }
      selectedProject.value.imageUrls.push(...uploadedUrls)
      detailForm.value.imageUrls = selectedProject.value.imageUrls
    }
    
    selectedProjectFiles.value = []
    if (projectFileInput.value) {
      projectFileInput.value.value = ''
    }
    await showAlert('사진이 성공적으로 업로드되었습니다.', '성공', 'success')
  } catch (error) {
    logError(error, '사진 업로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const deleteProjectPhoto = async (index) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')                                                                            
    if (confirmed) {
      const imageUrls = [...selectedProject.value.imageUrls]
      imageUrls.splice(index, 1)

      if (selectedProject.value.id) {
        const payload = buildProjectUpdatePayload({
          ...selectedProject.value,
          imageUrls
        })
        const updatedProjectResponse = await axios.put(`/projects/${selectedProject.value.id}`, payload)
        const updatedProject = updatedProjectResponse.data.data || updatedProjectResponse.data
        
        // 프로젝트 목록 새로고침
        await loadProjects()
        
        // 업데이트된 프로젝트 찾아서 selectedProject 업데이트
        const latestProject = projects.value.find(p => p.id === selectedProject.value.id)
        if (latestProject) {
          selectedProject.value = latestProject
          detailForm.value.imageUrls = latestProject.imageUrls || []
        } else {
          // 목록에서 찾을 수 없으면 서버 응답 사용
          selectedProject.value = updatedProject
          detailForm.value.imageUrls = updatedProject.imageUrls || []
        }
      } else {
        selectedProject.value.imageUrls = imageUrls
        detailForm.value.imageUrls = imageUrls
      }
    }
  } catch (error) {
    logError(error, '사진 삭제')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://') || imageUrl.startsWith('data:')) {
    return imageUrl
  }
  if (import.meta.env.DEV) {
    const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
    return `${API_BASE}${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`
  }
  return imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`
}

const deleteProject = async (id) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')
    if (confirmed) {
      await axios.delete(`/projects/${id}`)
      await showAlert('프로젝트가 삭제되었습니다.', '성공', 'success')
      await loadProjects()
    }
  } catch (error) {
    logError(error, '프로젝트 삭제')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
@import './common/admin-common.css';

.content-section {
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #1E1D1D;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.projects-list {
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.projects-header {
  margin-bottom: 1rem;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1E1D1D;
}

.projects-table {
  overflow-x: auto;
}

.projects-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.projects-table th,
.projects-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.projects-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.projects-table tr:hover {
  background: #f8f9fa;
}

.project-form-section,
.project-detail-form-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-top: 2rem;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.form-section {
  margin-bottom: 2rem;
  padding-bottom: 2rem;
  border-bottom: 1px solid #eee;
}

.form-section:last-child {
  border-bottom: none;
}

.form-section h3 {
  font-size: 1.2rem;
  margin-bottom: 1rem;
  color: #333;
}

.project-form,
.project-detail-form {
  max-width: 50rem;
}

.image-preview {
  margin-top: 0.5rem;
}

.image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 0.5rem;
  margin-top: 0.5rem;
}

.url-slug-display {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.readonly-input {
  flex: 1;
  background-color: #f5f5f5;
  cursor: not-allowed;
  color: #666;
}

.readonly-input:focus {
  outline: none;
  border-color: #ddd;
}

.link-button {
  padding: 0.5rem 1rem;
  background-color: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 0.25rem;
  font-size: 0.875rem;
  white-space: nowrap;
  transition: background-color 0.2s;
}

.link-button:hover {
  background-color: #0056b3;
}

.form-hint {
  display: block;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #666;
  line-height: 1.5;
}

.form-hint code {
  background-color: #f5f5f5;
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-family: 'Josefin Sans', sans-serif;
  font-size: 0.875rem;
  color: #333;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
}

.image-count {
  margin-top: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

.form-help-text {
  margin-top: 0.5rem;
  color: #999;
  font-size: 0.875rem;
  font-style: italic;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.photo-item {
  position: relative;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  overflow: hidden;
}

.photo-image-wrapper {
  cursor: pointer;
  transition: opacity 0.2s;
}

.photo-image-wrapper:hover {
  opacity: 0.8;
}

.photo-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  display: block;
}

.photo-actions {
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.7);
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}

.no-photos {
  text-align: center;
  padding: 2rem;
  color: #999;
  margin-bottom: 2rem;
}

.photo-upload {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.photo-upload h4 {
  margin-bottom: 1rem;
}

/* 리뷰 섹션 스타일 */
.reviews-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.review-item-form {
  border: 1px solid #ddd;
  border-radius: 0.5rem;
  padding: 1.5rem;
  background-color: #f9f9f9;
}

.review-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.review-item-header label {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
}

/* 설명 섹션 스타일 */
.descriptions-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.description-item-form {
  border: 1px solid #ddd;
  border-radius: 0.5rem;
  padding: 1.5rem;
  background-color: #f9f9f9;
}

.description-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.description-item-header label {
  font-weight: 600;
  color: #333;
  font-size: 1rem;
}
</style>

