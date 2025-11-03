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
      <div class="projects-header">
        <h2>전체 목록</h2>
        <BaseButton variant="primary" @click="showProjectDetailForm = true; editingProject = null">
          프로젝트 상세 등록
        </BaseButton>
      </div>
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
            <tr v-for="(project, index) in projects" :key="project.id">
              <td>{{ index + 1 }}</td>
              <td>{{ project.title }}</td>
              <td>{{ project.subtitle || '-' }}</td>
              <td>{{ project.location || '-' }}</td>
              <td>{{ formatDate(project.premiereDate) }}</td>
              <td>
                <div class="action-buttons">
                  <BaseButton size="small" variant="secondary" @click="editProject(project)">수정</BaseButton>
                  <BaseButton size="small" variant="info" @click="editProjectDetail(project)">상세 수정</BaseButton>
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
          <label>URL Slug (자세히 보기 링크용)</label>
          <input v-model="form.urlSlug" placeholder="예: arturo-ui" />
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
            <label>URL Slug (자세히 보기 링크용) *</label>
            <input v-model="detailForm.urlSlug" required placeholder="예: arturo-ui" />
          </div>
        </div>

        <!-- 설명 -->
        <div class="form-section">
          <h3>설명</h3>
          <div class="form-group">
            <label>설명 1</label>
            <textarea v-model="detailForm.description1" rows="4"></textarea>
          </div>
          <div class="form-group">
            <label>설명 2</label>
            <textarea v-model="detailForm.description2" rows="4"></textarea>
          </div>
          <div class="form-group">
            <label>설명 3</label>
            <textarea v-model="detailForm.description3" rows="4"></textarea>
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
            <label>대표 사진 (Main Image) *</label>
            <input type="file" @change="(e) => handleDetailImageChange(e, 'mainImage')" accept="image/*" />
            <div v-if="detailForm.mainImageUrl" class="image-preview">
              <img :src="detailForm.mainImageUrl" alt="대표 사진" />
            </div>
            <div v-else-if="uploadedDetailImages.mainImageUrl" class="image-preview">
              <img :src="uploadedDetailImages.mainImageUrl" alt="대표 사진" />
            </div>
          </div>
          
          <div class="form-row">
          <div class="form-group">
            <label>가로 이미지 1 (Horizontal 1)</label>
            <input type="file" @change="(e) => handleDetailImageChange(e, 'horizontal1Image')" accept="image/*" />
              <div v-if="detailForm.horizontal1ImageUrl" class="image-preview">
                <img :src="detailForm.horizontal1ImageUrl" alt="가로 이미지 1" />
              </div>
              <div v-else-if="uploadedDetailImages.horizontal1ImageUrl" class="image-preview">
                <img :src="uploadedDetailImages.horizontal1ImageUrl" alt="가로 이미지 1" />
              </div>
            </div>
          <div class="form-group">
            <label>가로 이미지 2 (Horizontal 2)</label>
            <input type="file" @change="(e) => handleDetailImageChange(e, 'horizontal2Image')" accept="image/*" />
              <div v-if="detailForm.horizontal2ImageUrl" class="image-preview">
                <img :src="detailForm.horizontal2ImageUrl" alt="가로 이미지 2" />
              </div>
              <div v-else-if="uploadedDetailImages.horizontal2ImageUrl" class="image-preview">
                <img :src="uploadedDetailImages.horizontal2ImageUrl" alt="가로 이미지 2" />
              </div>
            </div>
          </div>

          <div class="form-row">
          <div class="form-group">
            <label>세로 이미지 1 (Vertical 1)</label>
            <input type="file" @change="(e) => handleDetailImageChange(e, 'vertical1Image')" accept="image/*" />
              <div v-if="detailForm.vertical1ImageUrl" class="image-preview">
                <img :src="detailForm.vertical1ImageUrl" alt="세로 이미지 1" />
              </div>
              <div v-else-if="uploadedDetailImages.vertical1ImageUrl" class="image-preview">
                <img :src="uploadedDetailImages.vertical1ImageUrl" alt="세로 이미지 1" />
              </div>
            </div>
          <div class="form-group">
            <label>세로 이미지 2 (Vertical 2)</label>
            <input type="file" @change="(e) => handleDetailImageChange(e, 'vertical2Image')" accept="image/*" />
              <div v-if="detailForm.vertical2ImageUrl" class="image-preview">
                <img :src="detailForm.vertical2ImageUrl" alt="세로 이미지 2" />
              </div>
              <div v-else-if="uploadedDetailImages.vertical2ImageUrl" class="image-preview">
                <img :src="uploadedDetailImages.vertical2ImageUrl" alt="세로 이미지 2" />
              </div>
            </div>
          </div>
        </div>

        <!-- 리뷰 -->
        <div class="form-section">
          <h3>리뷰</h3>
          <div class="form-group">
            <label>리뷰 1 텍스트</label>
            <textarea v-model="detailForm.review1Text" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>리뷰 1 출처</label>
            <input v-model="detailForm.review1Source" />
          </div>
          <div class="form-group">
            <label>리뷰 2 텍스트</label>
            <textarea v-model="detailForm.review2Text" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>리뷰 2 출처</label>
            <input v-model="detailForm.review2Source" />
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '../../api/axios'
import { BaseButton, ConfirmDialog, AlertDialog } from '../common'
import { formatDate } from '../../utils/commonHelpers'
import { logError, getErrorMessage } from '../../utils/errorHandler'

// Reactive data
const projects = ref([])
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
  urlSlug: '',
  coverImageUrl: null
})
const detailForm = ref({
  title: '',
  subtitle: '',
  premiereDate: '',
  location: '',
  director: '',
  urlSlug: '',
  description1: '',
  description2: '',
  description3: '',
  thankYouText: '',
  moreInfoUrl: '',
  mainImageUrl: null,
  horizontal1ImageUrl: null,
  horizontal2ImageUrl: null,
  vertical1ImageUrl: null,
  vertical2ImageUrl: null,
  review1Text: '',
  review1Source: '',
  review2Text: '',
  review2Source: ''
})
const editingProject = ref(null)
const editingProjectDetail = ref(null)
const showProjectDetailForm = ref(false)
const uploadedCoverImageUrl = ref(null)
const uploadedDetailImages = ref({
  mainImageUrl: null,
  horizontal1ImageUrl: null,
  horizontal2ImageUrl: null,
  vertical1ImageUrl: null,
  vertical2ImageUrl: null
})

// 다이얼로그 상태
const confirmDialog = ref({
  isVisible: false,
  title: '확인',
  message: '',
  confirmText: '확인',
  cancelText: '취소',
  confirmVariant: 'danger',
  resolve: null,
  reject: null
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
  return new Promise((resolve, reject) => {
    confirmDialog.value = {
      isVisible: true,
      title,
      message,
      confirmText: '확인',
      cancelText: '취소',
      confirmVariant: 'danger',
      resolve,
      reject
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
  if (confirmDialog.value.reject) {
    confirmDialog.value.reject(false)
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
    return response.data.data?.url || response.data.url
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

const handleDetailImageChange = async (event, imageType) => {
  const file = event.target.files[0]
  if (file) {
    try {
      const url = await uploadImage(file)
      const fieldMap = {
        mainImage: 'mainImageUrl',
        horizontal1Image: 'horizontal1ImageUrl',
        horizontal2Image: 'horizontal2ImageUrl',
        vertical1Image: 'vertical1ImageUrl',
        vertical2Image: 'vertical2ImageUrl'
      }
      const fieldName = fieldMap[imageType]
      if (fieldName) {
        uploadedDetailImages.value[fieldName] = url
        detailForm.value[fieldName] = url
      }
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
  } catch (error) {
    logError(error, '프로젝트 로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const searchProjects = async () => {
  try {
    const params = {}
    if (searchFilters.value.title) params.title = searchFilters.value.title
    if (searchFilters.value.location) params.location = searchFilters.value.location
    if (searchFilters.value.startDate) params.startDate = searchFilters.value.startDate
    if (searchFilters.value.endDate) params.endDate = searchFilters.value.endDate
    
    const response = await axios.get('/projects', { params })
    projects.value = response.data.data || response.data || []
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
  loadProjects()
}

const editProject = (project) => {
  editingProject.value = project
  showProjectDetailForm.value = false
  form.value = {
    title: project.title || '',
    premiereDate: project.premiereDate || '',
    location: project.location || '',
    urlSlug: project.urlSlug || '',
    coverImageUrl: project.coverImageUrl || null
  }
  uploadedCoverImageUrl.value = project.coverImageUrl || null
}

const editProjectDetail = (project) => {
  editingProjectDetail.value = project
  showProjectDetailForm.value = true
  detailForm.value = {
    title: project.title || '',
    subtitle: project.subtitle || '',
    premiereDate: project.premiereDate || '',
    location: project.location || '',
    director: project.director || '',
    urlSlug: project.urlSlug || '',
    description1: project.description1 || '',
    description2: project.description2 || '',
    description3: project.description3 || '',
    thankYouText: project.thankYouText || '',
    moreInfoUrl: project.moreInfoUrl || '',
    mainImageUrl: project.mainImageUrl || null,
    horizontal1ImageUrl: project.horizontal1ImageUrl || null,
    horizontal2ImageUrl: project.horizontal2ImageUrl || null,
    vertical1ImageUrl: project.vertical1ImageUrl || null,
    vertical2ImageUrl: project.vertical2ImageUrl || null,
    review1Text: project.review1Text || '',
    review1Source: project.review1Source || '',
    review2Text: project.review2Text || '',
    review2Source: project.review2Source || ''
  }
  uploadedDetailImages.value = {
    mainImageUrl: project.mainImageUrl || null,
    horizontal1ImageUrl: project.horizontal1ImageUrl || null,
    horizontal2ImageUrl: project.horizontal2ImageUrl || null,
    vertical1ImageUrl: project.vertical1ImageUrl || null,
    vertical2ImageUrl: project.vertical2ImageUrl || null
  }
}

const cancelEdit = () => {
  editingProject.value = null
  form.value = {
    title: '',
    premiereDate: '',
    location: '',
    urlSlug: '',
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
    description1: '',
    description2: '',
    description3: '',
    thankYouText: '',
    moreInfoUrl: '',
    mainImageUrl: null,
    horizontal1ImageUrl: null,
    horizontal2ImageUrl: null,
    vertical1ImageUrl: null,
    vertical2ImageUrl: null,
    review1Text: '',
    review1Source: '',
    review2Text: '',
    review2Source: ''
  }
  uploadedDetailImages.value = {
    mainImageUrl: null,
    horizontal1ImageUrl: null,
    horizontal2ImageUrl: null,
    vertical1ImageUrl: null,
    vertical2ImageUrl: null
  }
}

const saveProject = async () => {
  try {
    const projectData = {
      title: form.value.title,
      premiereDate: form.value.premiereDate,
      location: form.value.location,
      urlSlug: form.value.urlSlug || form.value.title.toLowerCase().replace(/\s+/g, '-'),
      coverImageUrl: form.value.coverImageUrl || uploadedCoverImageUrl.value
    }

    if (editingProject.value) {
      await axios.put(`/projects/${editingProject.value.id}`, projectData)
      await showAlert('프로젝트가 수정되었습니다.', '성공', 'success')
    } else {
      await axios.post('/projects', projectData)
      await showAlert('프로젝트가 등록되었습니다.', '성공', 'success')
    }
    
    cancelEdit()
    await loadProjects()
  } catch (error) {
    logError(error, '프로젝트 저장')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const saveProjectDetail = async () => {
  try {
    const projectData = {
      title: detailForm.value.title,
      subtitle: detailForm.value.subtitle,
      premiereDate: detailForm.value.premiereDate,
      location: detailForm.value.location,
      director: detailForm.value.director,
      urlSlug: detailForm.value.urlSlug,
      description1: detailForm.value.description1,
      description2: detailForm.value.description2,
      description3: detailForm.value.description3,
      thankYouText: detailForm.value.thankYouText,
      moreInfoUrl: detailForm.value.moreInfoUrl,
      mainImageUrl: detailForm.value.mainImageUrl || uploadedDetailImages.value.mainImageUrl,
      horizontal1ImageUrl: detailForm.value.horizontal1ImageUrl || uploadedDetailImages.value.horizontal1ImageUrl,
      horizontal2ImageUrl: detailForm.value.horizontal2ImageUrl || uploadedDetailImages.value.horizontal2ImageUrl,
      vertical1ImageUrl: detailForm.value.vertical1ImageUrl || uploadedDetailImages.value.vertical1ImageUrl,
      vertical2ImageUrl: detailForm.value.vertical2ImageUrl || uploadedDetailImages.value.vertical2ImageUrl,
      review1Text: detailForm.value.review1Text,
      review1Source: detailForm.value.review1Source,
      review2Text: detailForm.value.review2Text,
      review2Source: detailForm.value.review2Source
    }

    if (editingProjectDetail.value) {
      await axios.put(`/projects/${editingProjectDetail.value.id}`, projectData)
      await showAlert('프로젝트 상세가 수정되었습니다.', '성공', 'success')
    } else {
      await axios.post('/projects', projectData)
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
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

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
}
</style>

