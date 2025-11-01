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
             <LazyImage
               :src="getImageUrl(photo.imageUrl)"
               :alt="photo.altText || 'Photo'"
               width="150px"
               height="150px"
               image-class="photo-image"
               placeholder-class="photo-placeholder"
             />
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
import { ref, onMounted, computed } from 'vue'
import { useMediaStore } from '../../../stores'
import { photoService } from '../../../services'
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

// Computed properties
const photoGroups = computed(() => mediaStore.photoGroups)

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
const searchFilters = ref(createPhotoGroupSearchFilters())
const form = ref(createPhotoGroupForm())
const editingGroup = ref(null)
const selectedGroup = ref(null)
const fileInput = ref(null)
const selectedFiles = ref([])

// 메서드
const loadPhotoGroups = async () => {
  try {
    await mediaStore.loadPhotoGroups()
  } catch (error) {
    console.error('사진 그룹 로드 실패:', error)
  }
}

const searchPhotoGroups = async () => {
  try {
    await mediaStore.loadPhotoGroups()
    // 검색은 클라이언트 사이드에서 처리 (computed로 필터링 가능)
  } catch (error) {
    console.error('사진 그룹 검색 실패:', error)
  }
}

const resetFilters = () => {
  resetPhotoGroupSearchFilters(searchFilters.value)
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

const getImageUrl = (fileUrl) => {
  if (!fileUrl) {
    return getPlaceholderImage(150, 150)
  }
  
  return getOptimizedImageUrl(fileUrl, { width: 150, height: 150 })
}

onMounted(() => {
  loadPhotoGroups()
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
}

.photo-item {
  position: relative;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  overflow: hidden;
}

.photo-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
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
</style>
