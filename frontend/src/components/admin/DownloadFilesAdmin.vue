<template>
  <div class="content-section">
    <h1 class="section-title">Download Files 관리</h1>
    
    <!-- 검색/필터 섹션 -->
    <SearchFilters
      v-model="searchFilters"
      :filters="searchFilterConfig"
      @search="searchFiles"
      @reset="resetFilters"
    />

    <!-- 파일 목록 -->
    <DataTable
      title="전체 목록"
      :data="displayedFiles"
      :columns="tableColumns"
      :actions="tableActions"
      @action="handleTableAction"
    >
      <template #cell-no="{ index }">
        {{ index + 1 }}
      </template>
    </DataTable>

    <!-- 파일 등록/수정 폼 -->
    <CrudForm
      ref="crudFormRef"
      title="파일"
      :fields="formFields"
      v-model="form"
      :editing-item="editingFile"
      @submit="saveFile"
      @cancel="cancelEdit"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from '../../api/axios'
import { downloadFileService } from '../../services'
import SearchFilters from './common/SearchFilters.vue'
import DataTable from './common/DataTable.vue'
import CrudForm from './common/CrudForm.vue'
import {
  createDownloadFileSearchFilters,
  createDownloadFileForm,
  resetDownloadFileSearchFilters,
  resetDownloadFileForm
} from '../../types/dto'

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'name', label: '파일명', placeholder: '파일명을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'no', label: 'No' },
  { key: 'name', label: '파일명' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'name', label: '파일명', type: 'text', required: true, placeholder: '파일명을 입력하세요' },
  { key: 'fileUrl', label: '파일 업로드', type: 'file', required: true },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const files = ref([])
const searchFilters = ref(createDownloadFileSearchFilters())
const form = ref(createDownloadFileForm())
const editingFile = ref(null)
const crudFormRef = ref(null)
const filteredFiles = ref([])
const isFiltered = ref(false)
const displayedFiles = computed(() =>
  isFiltered.value ? filteredFiles.value : files.value
)

// 메서드
const loadFiles = async () => {
  try {
    files.value = await downloadFileService.getAllDownloadFiles()
    if (isFiltered.value) {
      await searchFiles()
    }
  } catch (error) {
    console.error('파일 로드 실패:', error)
  }
}

const searchFiles = async () => {
  try {
    filteredFiles.value = await downloadFileService.searchDownloadFiles(searchFilters.value)
    isFiltered.value = true
  } catch (error) {
    console.error('파일 검색 실패:', error)
  }
}

const resetFilters = () => {
  resetDownloadFileSearchFilters(searchFilters.value)
  isFiltered.value = false
  filteredFiles.value = []
  loadFiles()
}

const handleTableAction = (action, item) => {
  switch (action) {
    case 'edit':
      editFile(item)
      break
    case 'delete':
      deleteFile(item.id)
      break
  }
}

const editFile = (file) => {
  editingFile.value = file
  form.value = {
    name: file.name,
    fileUrl: file.fileUrl || '',
    displayOrder: file.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingFile.value = null
  resetDownloadFileForm(form.value)
}

const saveFile = async () => {
  try {
    // 파일이 선택된 경우 먼저 업로드
    const fileObject = crudFormRef.value?.getFileObject('fileUrl')
    
    if (fileObject) {
      // FormData로 파일 업로드
      const formData = new FormData()
      formData.append('file', fileObject)
      
      // 파일 업로드 API 호출 (axios 인터셉터에서 FormData 자동 처리)
      const uploadResponse = await axios.post('/upload/file', formData)
      
      // 업로드된 파일의 S3 URL 저장
      if (uploadResponse.data && uploadResponse.data.url) {
        form.value.fileUrl = uploadResponse.data.url
      } else if (uploadResponse.data && uploadResponse.data.data && uploadResponse.data.data.url) {
        form.value.fileUrl = uploadResponse.data.data.url
      }
      
      // 파일 객체 제거
      crudFormRef.value?.clearFileObject('fileUrl')
    }
    
    // 파일 정보 저장
    if (editingFile.value) {
      // 수정
      await downloadFileService.updateDownloadFile(editingFile.value.id, form.value)
    } else {
      // 등록
      await downloadFileService.createDownloadFile(form.value)
    }
    
    await loadFiles()
    cancelEdit()
  } catch (error) {
    console.error('파일 저장 실패:', error)
    alert('파일 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteFile = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await downloadFileService.deleteDownloadFile(id)
      await loadFiles()
    } catch (error) {
      console.error('파일 삭제 실패:', error)
    }
  }
}

// Lifecycle
onMounted(() => {
  loadFiles()
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
</style>
