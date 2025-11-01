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
      :data="photoGroups"
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
         <div class="photos-grid">
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
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useMediaStore } from '../../../stores'
import { photoService } from '../../../services'
import { filterData } from '../../../utils'
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

// Computed properties - 검색 필터 적용
const photoGroups = computed(() => {
  const allGroups = mediaStore.photoGroups
  return filterData(allGroups, searchFilters.value)
})

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

// 메서드
const loadPhotoGroups = async () => {
  try {
    await mediaStore.loadPhotoGroups()
  } catch (error) {
    console.error('사진 그룹 로드 실패:', error)
  }
}

const searchPhotoGroups = async () => {
  // 검색은 computed로 자동 필터링되므로 별도 처리 불필요
}

const resetFilters = () => {
  resetPhotoGroupSearchFilters(searchFilters.value)
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
  } catch (error) {
    console.error('그룹 저장 실패:', error)
    alert('그룹 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteGroup = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await mediaStore.deletePhotoGroup(id)
    } catch (error) {
      console.error('그룹 삭제 실패:', error)
      alert('그룹 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message))
    }
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
    alert('파일을 선택해주세요.')
    return
  }

  try {
    await photoService.uploadPhotos(selectedGroup.value.id, selectedFiles.value)
    await mediaStore.loadPhotoGroups()
    // 그룹 정보 갱신
    if (selectedGroup.value) {
      const updatedGroup = await photoService.getPhotoGroup(selectedGroup.value.id)
      selectedGroup.value = updatedGroup
    }
    selectedFiles.value = []
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    alert('사진이 성공적으로 업로드되었습니다.')
  } catch (error) {
    console.error('사진 업로드 실패:', error)
    alert('사진 업로드에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deletePhoto = async (photoId) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await photoService.deletePhoto(photoId)
      await mediaStore.loadPhotoGroups()
      // 그룹 정보 갱신
      if (selectedGroup.value) {
        const updatedGroup = await photoService.getPhotoGroup(selectedGroup.value.id)
        selectedGroup.value = updatedGroup
      }
    } catch (error) {
      console.error('사진 삭제 실패:', error)
      alert('사진 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message))
    }
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

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
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
