<template>
  <div class="photo-tab">
    <!-- 검색/필터 섹션 -->
    <SearchFilters
      v-model="searchFilters"
      :filters="searchFilterConfig"
      @search="searchPhotoGroups"
      @reset="resetFilters"
    />

    <!-- 사진 그룹 목록 -->
    <DataTable
      title="전체 목록"
      :data="displayedPhotoGroups"
      :columns="tableColumns"
      :actions="tableActions"
      @action="handleTableAction"
    >
      <template #cell-photos="{ value }">
        {{ value ? value.length : 0 }}
      </template>
    </DataTable>

    <!-- 사진 관리 모달 -->
    <Modal
      :is-visible="!!selectedGroup"
      :title="selectedGroup ? `${selectedGroup.title} - 사진 관리` : ''"
      @close="closeModal"
    >
      <div v-if="selectedGroup">
        <!-- 사진 목록 -->
        <div v-if="!selectedGroup.photos || selectedGroup.photos.length === 0" class="empty-photos">
          <p>등록된 사진이 없습니다.</p>
        </div>
        <div v-else class="photos-grid">
          <div v-for="photo in selectedGroup.photos" :key="photo.id" class="photo-item">
            <div class="photo-image-wrapper" @click="openImageModal(photo)">
              <LazyImage
                :src="getImageUrl(photo)"
                :alt="photo.altText || 'Photo'"
                width="150px"
                height="150px"
                image-class="photo-image"
                placeholder-class="photo-placeholder"
              />
            </div>
            <div class="photo-actions">
              <button class="btn-delete-small" @click="deletePhoto(photo.id)">삭제</button>
            </div>
          </div>
        </div>

        <!-- 사진 업로드 -->
        <div class="photo-upload">
          <h4>사진 추가</h4>
          <input type="file" ref="fileInput" multiple @change="handleFileSelect" accept="image/*" />
          <button class="btn-upload" @click="uploadPhotos">업로드</button>
        </div>
      </div>
    </Modal>

    <!-- 원본 이미지 보기 모달 -->
    <Modal
      :is-visible="!!selectedPhoto"
      :title="selectedPhoto ? '원본 이미지' : ''"
      :show-footer="false"
      @close="closeImageModal"
    >
      <div v-if="selectedPhoto" class="image-viewer">
        <img 
          :src="getOriginalImageUrl(selectedPhoto)" 
          :alt="selectedPhoto.altText || 'Photo'"
          class="full-size-image"
          @load="onImageLoad"
          @error="onImageError"
        />
        <div v-if="imageLoading" class="image-loading">이미지 로딩 중...</div>
        <div v-if="imageError" class="image-error">이미지를 불러올 수 없습니다.</div>
      </div>
    </Modal>

    <!-- 사진 그룹 등록/수정 폼 -->
    <CrudForm
      title="그룹"
      :fields="formFields"
      v-model="form"
      :editing-item="editingGroup"
      @submit="saveGroup"
      @cancel="cancelEdit"
    />

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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useMediaStore } from '../../../stores'
import { photoService } from '../../../services'
import { logError, getErrorMessage } from '../../../utils/errorHandler'
import { ConfirmDialog, AlertDialog } from '../../common'
import LazyImage from '../../common/LazyImage.vue'
import { 
  createPhotoGroupSearchFilters, 
  createPhotoGroupForm,
  resetPhotoGroupSearchFilters,
  resetPhotoGroupForm
} from '../../../types/dto/photo'
import { getOptimizedImageUrl, getPlaceholderImage } from '../../../utils/imageOptimization'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'
import Modal from '../common/Modal.vue'

// 스토어 사용
const mediaStore = useMediaStore()

// 검색 필터
const searchFilters = ref(createPhotoGroupSearchFilters())

// 사진 그룹 목록
const isFiltered = ref(false)
const filteredPhotoGroups = ref([])

const displayedPhotoGroups = computed(() => (
  isFiltered.value ? filteredPhotoGroups.value : mediaStore.photoGroups
))

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '그룹명', placeholder: '그룹명을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'title', label: '그룹명' },
  { key: 'photos', label: '사진 수' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'manage', label: '사진 관리', class: 'btn-manage' },
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'title', label: '그룹명', type: 'text', required: true, placeholder: '그룹명을 입력하세요' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const form = ref(createPhotoGroupForm())
const editingGroup = ref(null)
const selectedGroup = ref(null)
const fileInput = ref(null)
const selectedFiles = ref([])
const selectedPhoto = ref(null)
const imageLoading = ref(false)
const imageError = ref(false)

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

// 메서드
const loadPhotoGroups = async () => {
  try {
    await mediaStore.loadPhotoGroups()
    if (!isFiltered.value) {
      filteredPhotoGroups.value = []
    }
  } catch (error) {
    logError(error, '사진 그룹 로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const searchPhotoGroups = async () => {
  try {
    const results = await photoService.searchPhotoGroups(searchFilters.value)
    filteredPhotoGroups.value = results
    isFiltered.value = true
  } catch (error) {
    logError(error, '사진 그룹 검색')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const resetFilters = () => {
  resetPhotoGroupSearchFilters(searchFilters.value)
  isFiltered.value = false
  filteredPhotoGroups.value = []
  loadPhotoGroups()
}

const handleTableAction = (action, item) => {
  switch (action) {
    case 'manage':
      managePhotos(item)
      break
    case 'edit':
      editGroup(item)
      break
    case 'delete':
      deleteGroup(item.id)
      break
  }
}

const editGroup = (group) => {
  editingGroup.value = group
  form.value = {
    title: group.title,
    displayOrder: group.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingGroup.value = null
  resetPhotoGroupForm(form.value)
}

const saveGroup = async () => {
  try {
    if (editingGroup.value) {
      await mediaStore.updatePhotoGroup(editingGroup.value.id, form.value)
    } else {
      await mediaStore.addPhotoGroup(form.value)
    }
    
    cancelEdit()
    if (isFiltered.value) {
      await searchPhotoGroups()
    }
  } catch (error) {
    logError(error, '그룹 저장')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const deleteGroup = async (id) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')
    if (confirmed) {
      await mediaStore.deletePhotoGroup(id)
      if (isFiltered.value) {
        await searchPhotoGroups()
      }
    }
  } catch (error) {
    logError(error, '그룹 삭제')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const managePhotos = async (group) => {
  selectedGroup.value = group
  selectedFiles.value = []
}

const closeModal = () => {
  selectedGroup.value = null
  selectedFiles.value = []
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const handleFileSelect = (event) => {
  selectedFiles.value = Array.from(event.target.files)
}

const uploadPhotos = async () => {
  if (!selectedFiles.value.length || !selectedGroup.value) {
    await showAlert('파일을 선택해주세요.', '알림', 'warning')
    return
  }

  try {
    // 사진 업로드
    const uploadedPhotos = await photoService.uploadPhotos(selectedGroup.value.id, selectedFiles.value)
    
    // 전체 그룹 목록 갱신
    await mediaStore.loadPhotoGroups()
    
    // 선택된 그룹 정보 갱신 (업로드된 사진이 포함된 최신 정보)
    if (selectedGroup.value) {
      try {
        // 최신 그룹 정보 가져오기
        const updatedGroup = await photoService.getPhotoGroup(selectedGroup.value.id)
        if (updatedGroup) {
          // photos 배열이 없으면 빈 배열로 초기화
          if (!updatedGroup.photos) {
            updatedGroup.photos = []
          }
          // 업로드된 사진이 있으면 추가 (중복 방지)
          if (uploadedPhotos && Array.isArray(uploadedPhotos) && uploadedPhotos.length > 0) {
            // 이미 존재하는 사진 ID 체크하여 중복 방지
            const existingIds = new Set((updatedGroup.photos || []).map(p => p.id))
            uploadedPhotos.forEach(photo => {
              if (photo.id && !existingIds.has(photo.id)) {
                updatedGroup.photos.push(photo)
              }
            })
          }
          selectedGroup.value = updatedGroup
        } else {
          // getPhotoGroup이 실패하면 스토어에서 최신 정보 가져오기
          const latestGroup = mediaStore.photoGroups.find(g => g.id === selectedGroup.value.id)
          if (latestGroup) {
            selectedGroup.value = latestGroup
          }
        }
      } catch (updateError) {
        console.warn('그룹 정보 갱신 실패, 스토어에서 가져오기:', updateError)
        // 스토어에서 최신 정보 가져오기
        const latestGroup = mediaStore.photoGroups.find(g => g.id === selectedGroup.value.id)
        if (latestGroup) {
          selectedGroup.value = latestGroup
        }
      }
    }
    
    // 파일 선택 초기화
    selectedFiles.value = []
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    
    await showAlert('사진이 성공적으로 업로드되었습니다.', '성공', 'success')
    if (isFiltered.value) {
      await searchPhotoGroups()
    }
  } catch (error) {
    logError(error, '사진 업로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const deletePhoto = async (photoId) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')
    if (confirmed) {
      await photoService.deletePhoto(photoId)
      await mediaStore.loadPhotoGroups()
      
      // 그룹 정보 갱신
      if (selectedGroup.value) {
        try {
          const updatedGroup = await photoService.getPhotoGroup(selectedGroup.value.id)
          if (updatedGroup && updatedGroup.photos) {
            selectedGroup.value = updatedGroup
          } else {
            // 스토어에서 최신 정보 가져오기
        const latestGroup = mediaStore.photoGroups.find(g => g.id === selectedGroup.value.id)
            if (latestGroup) {
              selectedGroup.value = latestGroup
            }
          }
        } catch (updateError) {
          console.warn('그룹 정보 갱신 실패, 스토어에서 가져오기:', updateError)
          // 스토어에서 최신 정보 가져오기
          const latestGroup = mediaStore.photoGroups.find(g => g.id === selectedGroup.value.id)
          if (latestGroup) {
            selectedGroup.value = latestGroup
          }
        }
      }
      if (isFiltered.value) {
        await searchPhotoGroups()
      }
    }
  } catch (error) {
    logError(error, '사진 삭제')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const getImageUrl = (photo) => {
  // 썸네일 URL이 있으면 우선 사용, 없으면 원본 이미지 URL 사용
  const imageUrl = photo.thumbnailUrl || photo.imageUrl
  if (!imageUrl) {
    return getPlaceholderImage(150, 150)
  }
  
  return getOptimizedImageUrl(imageUrl, { width: 150, height: 150 })
}

const getOriginalImageUrl = (photo) => {
  // 원본 이미지 URL 사용 (썸네일이 아닌)
  if (!photo || !photo.imageUrl) {
    return getPlaceholderImage(800, 600)
  }
  
  return getOptimizedImageUrl(photo.imageUrl)
}

const openImageModal = (photo) => {
  selectedPhoto.value = photo
  imageLoading.value = true
  imageError.value = false
}

const closeImageModal = () => {
  selectedPhoto.value = null
  imageLoading.value = false
  imageError.value = false
}

const onImageLoad = () => {
  imageLoading.value = false
  imageError.value = false
}

const onImageError = () => {
  imageLoading.value = false
  imageError.value = true
}

// ESC 키로 모달 닫기
const handleKeyDown = (event) => {
  if (event.key === 'Escape' && selectedPhoto.value) {
    closeImageModal()
  }
}

onMounted(() => {
  loadPhotoGroups()
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
@import '../common/admin-common.css';

.photo-tab {
  padding: 0;
}

.empty-photos {
  text-align: center;
  padding: 3rem 1rem;
  color: #999;
  font-size: 1rem;
}

.empty-photos p {
  margin: 0;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.3rem;
  margin-bottom: 1rem;
  /* 이미지 로딩 시 레이아웃 시프트 방지 */
  contain: layout;
}

.photo-item {
  position: relative;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  overflow: hidden;
  /* 이미지 로딩 최적화 */
  contain: layout style;
  will-change: contents;
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
  /* 이미지 로딩 성능 최적화 */
  image-rendering: auto;
  /* GPU 가속으로 스크롤 성능 개선 */
  transform: translateZ(0);
  backface-visibility: hidden;
  /* 이미지 로딩 최적화 */
  loading: lazy;
}

.photo-placeholder {
  width: 100%;
  height: 150px;
}

.photo-actions {
  padding: 0.5rem;
  background: rgba(0, 0, 0, 0.7);
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}

.photo-upload {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid #eee;
}

.photo-upload h4 {
  margin-bottom: 1rem;
}

/* 원본 이미지 뷰어 */
.image-viewer {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #f5f5f5;
}

.full-size-image {
  max-width: 100%;
  max-height: 80vh;
  height: auto;
  object-fit: contain;
  border-radius: 0.25rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.image-loading,
.image-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  padding: 1rem 2rem;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  border-radius: 0.25rem;
  font-size: 0.9rem;
}

.image-error {
  background: rgba(220, 53, 69, 0.9);
}
</style>
