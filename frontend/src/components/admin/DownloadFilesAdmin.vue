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

    <div class="download-mode">
      <label for="download-source">등록 방식</label>
      <select id="download-source" v-model="downloadSource" @change="changeDownloadSource">
        <option value="dropbox">Dropbox 공유 링크</option>
        <option value="upload">서버 파일 업로드</option>
      </select>
      <p v-if="downloadSource === 'dropbox'">Dropbox에 파일 또는 폴더를 업로드한 후, 로그인 없이 열리고 다운로드 가능한 공유 링크를 등록하세요. 자료에 저작권과 크레딧을 유지하세요.</p>
    </div>
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
import { downloadFileService, uploadService } from '../../services'
import { isDropboxUrl } from '../../utils/downloadLinks'
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
const downloadSource = ref('dropbox')
const formFields = computed(() => [
  { key: 'name', label: '파일명', type: 'text', required: true, placeholder: '파일명을 입력하세요' },
  downloadSource.value === 'dropbox'
    ? { key: 'fileUrl', label: 'Dropbox 공유 링크', type: 'url', required: true, placeholder: 'https://www.dropbox.com/scl/...' }
    : { key: 'fileUrl', label: '파일 업로드', type: 'file', required: true, accept: 'image/*,application/pdf' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
])

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

const changeDownloadSource = () => {
  form.value.fileUrl = ''
  crudFormRef.value?.clearFileObject('fileUrl')
}

const editFile = (file) => {
  crudFormRef.value?.clearFileObject('fileUrl')
  downloadSource.value = isDropboxUrl(file.fileUrl) ? 'dropbox' : 'upload'
  editingFile.value = file
  form.value = {
    name: file.name,
    fileUrl: file.fileUrl || '',
    displayOrder: file.displayOrder || 0
  }
}

const cancelEdit = () => {
  crudFormRef.value?.clearFileObject('fileUrl')
  downloadSource.value = 'dropbox'
  editingFile.value = null
  resetDownloadFileForm(form.value)
}

const saveFile = async () => {
  try {
    // 파일이 선택된 경우 먼저 업로드
    const fileObject = crudFormRef.value?.getFileObject('fileUrl')
    
    if (downloadSource.value === 'dropbox') {
      form.value.fileUrl = form.value.fileUrl.trim()
      if (!isDropboxUrl(form.value.fileUrl)) throw new Error('올바른 HTTPS Dropbox 공유 링크를 입력하세요.')
    } else if (fileObject) {
      form.value.fileUrl = await uploadService.uploadFile(fileObject)
      // 파일 객체 제거
      crudFormRef.value?.clearFileObject('fileUrl')
    }
    
    if (!form.value.fileUrl) throw new Error('파일 또는 공유 링크를 등록하세요.')
    const payload = { ...form.value, downloadSource: downloadSource.value }
    // 파일 정보 저장
    if (editingFile.value) {
      // 수정
      await downloadFileService.updateDownloadFile(editingFile.value.id, payload)
    } else {
      // 등록
      await downloadFileService.createDownloadFile(payload)
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
.download-mode { margin: 2rem 0 1rem; }
.download-mode select { margin-left: 1rem; padding: 0.6rem; font: inherit; }
.download-mode p { font-size: 0.85rem; color: #666; margin-top: 0.75rem; }



.content-section {
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #1E1D1D;
}

/* 헤더 */
.admin-header h2 {
  color: var(--color-text);
  font-size: 1.5rem;
  font-weight: 600;
}

/* 테이블 */
table th {
  background-color: var(--color-background);
  color: var(--color-text-secondary); /* #555 */
  font-weight: 500;
  text-align: left;
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
}

table td {
  padding: 1rem;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text);
}

/* 폼 */
.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-text);
}
</style>
