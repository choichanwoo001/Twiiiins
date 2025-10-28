<template>
  <div class="content-section">
    <h1 class="section-title">Download Files 관리</h1>
    
    <!-- 검색/필터 섹션 -->
    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label>파일명</label>
          <input v-model="searchFilters.name" placeholder="파일명을 입력하세요" />
        </div>
        <div class="filter-actions">
          <button class="btn-reset" @click="resetFilters">초기화</button>
          <button class="btn-search" @click="searchFiles">검색</button>
        </div>
      </div>
    </div>

    <!-- 파일 목록 -->
    <div class="files-list">
      <h2>전체 목록</h2>
      <div class="files-table">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>파일명</th>
              <th>파일 URL</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(file, index) in files" :key="file.id">
              <td>{{ index + 1 }}</td>
              <td>{{ file.name }}</td>
              <td>{{ file.fileUrl }}</td>
              <td>
                <button class="btn-edit" @click="editFile(file)">수정</button>
                <button class="btn-delete" @click="deleteFile(file.id)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 파일 등록/수정 폼 -->
    <div class="file-form-section">
      <h2>{{ editingFile ? '파일 수정' : '새 파일 등록' }}</h2>
      <form @submit.prevent="saveFile" class="file-form">
        <div class="form-group">
          <label>파일명 *</label>
          <input v-model="form.name" required />
        </div>

        <div class="form-group">
          <label>파일 업로드 *</label>
          <div class="file-upload-container">
            <input 
              type="file" 
              ref="fileInput" 
              @change="handleFileUpload" 
              style="display: none"
            />
            <button type="button" class="btn-upload" @click="$refs.fileInput.click()">
              파일 선택
            </button>
            <span v-if="form.fileUrl" class="file-name">{{ form.fileUrl }}</span>
          </div>
        </div>

        <div class="form-group">
          <label>표시 순서</label>
          <input type="number" v-model="form.displayOrder" min="0" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-save">{{ editingFile ? '수정' : '등록' }}</button>
          <button type="button" class="btn-cancel" @click="cancelEdit" v-if="editingFile">취소</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { downloadFileService } from '../../services'
import {
  createDownloadFileSearchFilters,
  createDownloadFileForm,
  resetDownloadFileSearchFilters,
  resetDownloadFileForm
} from '../../types/dto'

// Props
const props = defineProps({
  files: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:files'])

// Reactive data
const searchFilters = ref(createDownloadFileSearchFilters())
const form = ref(createDownloadFileForm())
const editingFile = ref(null)

// Methods
const loadFiles = async () => {
  try {
    const files = await downloadFileService.getAllDownloadFiles()
    emit('update:files', files)
  } catch (error) {
    console.error('파일 로드 실패:', error)
  }
}

const searchFiles = async () => {
  try {
    const files = await downloadFileService.searchDownloadFiles(searchFilters.value)
    emit('update:files', files)
  } catch (error) {
    console.error('파일 검색 실패:', error)
  }
}

const resetFilters = () => {
  resetDownloadFileSearchFilters(searchFilters.value)
  loadFiles()
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

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 파일 업로드 로직 (실제 구현 필요)
    form.value.fileUrl = file.name
  }
}

// Lifecycle
onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.content-section {
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #333;
}

.search-section {
  background: white;
  padding: 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.search-filters {
  display: flex;
  gap: 1rem;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: #555;
}

.filter-group input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  font-size: 0.9rem;
}

.filter-actions {
  display: flex;
  gap: 0.5rem;
}

.files-list {
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.files-list h2 {
  margin-bottom: 1rem;
  color: #333;
}

.files-table {
  overflow-x: auto;
}

.files-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.files-table th,
.files-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.files-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.files-table tr:hover {
  background: #f8f9fa;
}

.file-form-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.file-form {
  max-width: 50rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.form-group label {
  font-weight: 500;
  color: #555;
}

.form-group input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  font-size: 1rem;
}

.file-upload-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-upload {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  font-weight: 500;
}

.btn-upload:hover {
  background: #0056b3;
}

.file-name {
  color: #666;
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

/* 버튼 스타일 */
.btn-reset, .btn-search {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset {
  background: #95a5a6;
  color: white;
}

.btn-reset:hover {
  background: #7f8c8d;
}

.btn-search {
  background: #3498db;
  color: white;
}

.btn-search:hover {
  background: #2980b9;
}

.btn-edit, .btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 0.5rem;
}

.btn-edit {
  background: #f39c12;
  color: white;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-delete {
  background: #943C31;
  color: white;
}

.btn-delete:hover {
  background: #7a2f26;
}

.btn-save {
  padding: 0.75rem 2rem;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover {
  background: #229954;
}

.btn-cancel {
  padding: 0.75rem 2rem;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #7f8c8d;
}
</style>
