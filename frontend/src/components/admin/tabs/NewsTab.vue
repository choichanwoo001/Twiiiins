<template>
  <div class="news-tab">
    <!-- 검색/필터 섹션 -->
    <SearchFilters
      v-model="searchFilters"
      :filters="searchFilterConfig"
      @search="searchNews"
      @reset="resetFilters"
    />

    <!-- 뉴스 목록 -->
    <DataTable
      title="전체 목록"
      :data="newsList"
      :columns="tableColumns"
      :actions="tableActions"
      @action="handleTableAction"
    >
      <template #cell-no="{ index }">
        {{ index + 1 }}
      </template>
    </DataTable>

    <!-- 뉴스 등록/수정 폼 -->
    <CrudForm
      title="뉴스"
      :fields="formFields"
      v-model="form"
      :editing-item="editingNews"
      @submit="saveNews"
      @cancel="cancelEdit"
    />

    <!-- 사진 관리 모달 -->
    <Modal
      :is-visible="!!selectedNews"
      :title="selectedNews ? `${selectedNews.title} - 사진 관리` : ''"
      @close="closeModal"
    >
      <div v-if="selectedNews">
        <!-- 사진 목록 -->
        <div class="photos-grid" v-if="selectedNews.imageUrls && selectedNews.imageUrls.length > 0">
          <div v-for="(imageUrl, index) in selectedNews.imageUrls" :key="index" class="photo-item">
            <div class="photo-image-wrapper">
              <img :src="getImageUrl(imageUrl)" :alt="`Photo ${index + 1}`" class="photo-image" />
            </div>
            <div class="photo-actions">
              <button class="btn-delete-small" @click="deletePhoto(index)">삭제</button>
            </div>
          </div>
        </div>
        <div v-else class="no-photos">
          등록된 사진이 없습니다.
        </div>

        <!-- 사진 업로드 -->
        <div class="photo-upload">
          <h4>사진 추가</h4>
          <input type="file" ref="fileInput" multiple @change="handleFileSelect" accept="image/*" />
          <button class="btn-upload" @click="uploadPhotos">업로드</button>
        </div>
      </div>
    </Modal>

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
import { ref, onMounted } from 'vue'
import { useMediaStore } from '../../../stores'
import { newsService } from '../../../services'
import { logError, getErrorMessage } from '../../../utils/errorHandler'
import { ConfirmDialog, AlertDialog } from '../../common'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'
import Modal from '../common/Modal.vue'

// 스토어 사용
const mediaStore = useMediaStore()

// 검색 필터
const searchFilters = ref({ title: '', startDate: '', endDate: '' })

// 뉴스 목록
const newsList = ref([])

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '제목', placeholder: '제목을 입력하세요' },
  {
    row: true,
    fields: [
      { key: 'startDate', label: '시작 날짜', type: 'date' },
      { key: 'endDate', label: '종료 날짜', type: 'date' }
    ]
  }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'no', label: 'No' },
  { key: 'date', label: '날짜' },
  { key: 'title', label: '제목' },
  { key: 'description', label: '설명' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'manage', label: '사진 관리', class: 'btn-manage' },
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'date', label: '날짜', type: 'date', required: true },
  { key: 'title', label: '제목', type: 'text', required: true, placeholder: '제목을 입력하세요' },
  { key: 'description', label: '설명', type: 'textarea', placeholder: '설명을 입력하세요' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const form = ref({ date: '', title: '', description: '', displayOrder: 0 })
const editingNews = ref(null)
const selectedNews = ref(null)
const fileInput = ref(null)
const selectedFiles = ref([])

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

// 메서드
const loadNews = async () => {
  try {
    await mediaStore.loadNews()
    newsList.value = mediaStore.newsItems
  } catch (error) {
    logError(error, '뉴스 로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const searchNews = async () => {
  try {
    const results = await newsService.searchNews(searchFilters.value)
    newsList.value = results
  } catch (error) {
    logError(error, '뉴스 검색')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const resetFilters = () => {
  searchFilters.value = { title: '', startDate: '', endDate: '' }
  loadNews()
}

const handleTableAction = (action, item) => {
  switch (action) {
    case 'manage':
      managePhotos(item)
      break
    case 'edit':
      editNews(item)
      break
    case 'delete':
      deleteNews(item.id)
      break
  }
}

const editNews = (news) => {
  editingNews.value = news
  form.value = {
    date: news.date || '',
    title: news.title || '',
    description: news.description || '',
    displayOrder: news.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingNews.value = null
  form.value = { date: '', title: '', description: '', displayOrder: 0 }
}

const saveNews = async () => {
  try {
    // 날짜가 없으면 오늘 날짜로 설정
    const newsData = {
      ...form.value,
      date: form.value.date || new Date().toISOString().split('T')[0]
    }
    
    if (editingNews.value) {
      await mediaStore.updateNews(editingNews.value.id, newsData)
    } else {
      await mediaStore.addNews(newsData)
    }
    
    cancelEdit()
    await loadNews()
  } catch (error) {
    logError(error, '뉴스 저장')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const deleteNews = async (id) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')
    if (confirmed) {
      await mediaStore.deleteNews(id)
      await loadNews()
    }
  } catch (error) {
    logError(error, '뉴스 삭제')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const managePhotos = async (news) => {
  selectedNews.value = news
  selectedFiles.value = []
}

const closeModal = () => {
  selectedNews.value = null
  selectedFiles.value = []
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleFileSelect = (event) => {
  selectedFiles.value = Array.from(event.target.files)
}

const uploadPhotos = async () => {
  if (!selectedFiles.value.length || !selectedNews.value) {
    await showAlert('파일을 선택해주세요.', '알림', 'warning')
    return
  }

  try {
    const updatedNews = await newsService.uploadNewsImages(selectedNews.value.id, selectedFiles.value)
    selectedNews.value = updatedNews
    selectedFiles.value = []
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    await loadNews()
    await showAlert('사진이 성공적으로 업로드되었습니다.', '성공', 'success')
  } catch (error) {
    logError(error, '사진 업로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const deletePhoto = async (index) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')
    if (confirmed) {
      const imageUrls = [...selectedNews.value.imageUrls]
      imageUrls.splice(index, 1)
      const updatedNews = await newsService.updateNews(selectedNews.value.id, {
        ...selectedNews.value,
        imageUrls
      })
      selectedNews.value = updatedNews
      await loadNews()
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

onMounted(() => {
  loadNews()
})

</script>

<style scoped>
@import '../common/admin-common.css';

.news-tab {
  padding: 0;
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
</style>
