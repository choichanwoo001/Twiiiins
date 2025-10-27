<template>
  <div class="photo-tab">
    <!-- 검색/필터 섹션 -->
    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label>그룹명</label>
          <input v-model="searchFilters.title" placeholder="그룹명을 입력하세요" />
        </div>
        <div class="filter-actions">
          <button class="btn-reset" @click="resetFilters">초기화</button>
          <button class="btn-search" @click="searchPhotoGroups">검색</button>
        </div>
      </div>
    </div>

    <!-- 사진 그룹 목록 -->
    <div class="photo-list">
      <h2>전체 목록</h2>
      <div class="photo-table">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>그룹명</th>
              <th>사진 수</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(group, index) in photoGroups" :key="group.id">
              <td>{{ index + 1 }}</td>
              <td>{{ group.title }}</td>
              <td>{{ group.photos ? group.photos.length : 0 }}</td>
              <td>
                <button class="btn-manage" @click="managePhotos(group)">사진 관리</button>
                <button class="btn-edit" @click="editGroup(group)">수정</button>
                <button class="btn-delete" @click="deleteGroup(group.id)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 사진 관리 모달 -->
    <div v-if="selectedGroup" class="photo-modal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ selectedGroup.title }} - 사진 관리</h3>
          <button class="btn-close" @click="closeModal">×</button>
        </div>
        
                 <!-- 사진 목록 -->
         <div class="photos-grid">
           <div v-for="photo in selectedGroup.photos" :key="photo.id" class="photo-item">
             <img :src="getImageUrl(photo.imageUrl)" :alt="photo.altText || 'Photo'" onerror="this.src='data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'150\' height=\'150\'%3E%3Crect width=\'150\' height=\'150\' fill=\'%23ddd\'/%3E%3Ctext x=\'50%25\' y=\'50%25\' text-anchor=\'middle\' dy=\'.3em\' fill=\'%23999\'%3ENo Image%3C/text%3E%3C/svg%3E'" />
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
    </div>

    <!-- 사진 그룹 등록/수정 폼 -->
    <div class="photo-form-section">
      <h2>{{ editingGroup ? '그룹 수정' : '새 그룹 등록' }}</h2>
      <form @submit.prevent="saveGroup" class="photo-form">
        <div class="form-group">
          <label>그룹명 *</label>
          <input v-model="form.title" required />
        </div>

        <div class="form-group">
          <label>표시 순서</label>
          <input type="number" v-model="form.displayOrder" min="0" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-save">{{ editingGroup ? '수정' : '등록' }}</button>
          <button type="button" class="btn-cancel" @click="cancelEdit" v-if="editingGroup">취소</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../../../api/axios'

const searchFilters = ref({ title: '' })
const form = ref({ title: '', displayOrder: 0 })
const photoGroups = ref([])
const editingGroup = ref(null)
const selectedGroup = ref(null)
const fileInput = ref(null)
const selectedFiles = ref([])

const loadPhotoGroups = async () => {
  try {
    const res = await axios.get('/api/media/photo-groups')
    photoGroups.value = res.data
  } catch (error) {
    console.error('사진 그룹 로드 실패:', error)
  }
}

const searchPhotoGroups = async () => {
  loadPhotoGroups()
}

const resetFilters = () => {
  searchFilters.value = { title: '' }
  loadPhotoGroups()
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
  form.value = { title: '', displayOrder: 0 }
}

const saveGroup = async () => {
  try {
    if (editingGroup.value) {
      await axios.put(`/api/media/photo-groups/${editingGroup.value.id}`, form.value)
    } else {
      await axios.post('/api/media/photo-groups', form.value)
    }
    
    await loadPhotoGroups()
    cancelEdit()
  } catch (error) {
    console.error('그룹 저장 실패:', error)
  }
}

const deleteGroup = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/photo-groups/${id}`)
      await loadPhotoGroups()
    } catch (error) {
      console.error('그룹 삭제 실패:', error)
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
  if (!selectedFiles.value.length || !selectedGroup.value) return

  try {
    const formData = new FormData()
    selectedFiles.value.forEach((file) => {
      formData.append('files', file)
    })

    await axios.post(`/api/media/photos/${selectedGroup.value.id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    await loadPhotoGroups()
    // 그룹 정보 갱신
    if (selectedGroup.value) {
      const res = await axios.get(`/api/media/photo-groups/${selectedGroup.value.id}`)
      selectedGroup.value = res.data
    }
    selectedFiles.value = []
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  } catch (error) {
    console.error('사진 업로드 실패:', error)
  }
}

const deletePhoto = async (photoId) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/photos/${photoId}`)
      await loadPhotoGroups()
      // 그룹 정보 갱신
      if (selectedGroup.value) {
        const res = await axios.get(`/api/media/photo-groups/${selectedGroup.value.id}`)
        selectedGroup.value = res.data
      }
    } catch (error) {
      console.error('사진 삭제 실패:', error)
    }
  }
}

const getImageUrl = (fileUrl) => {
  if (!fileUrl) {
    console.log('No fileUrl provided')
    return ''
  }
  
  console.log('Original fileUrl:', fileUrl)
  
  // 이미 전체 URL인 경우
  if (fileUrl.startsWith('http://') || fileUrl.startsWith('https://')) {
    console.log('Already full URL:', fileUrl)
    return fileUrl
  }
  
  // 상대 경로인 경우
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  console.log('Base URL:', baseURL)
  
  // fileUrl이 /uploads/... 형태가 아닌 경우 / 추가
  const path = fileUrl.startsWith('/') ? fileUrl : `/${fileUrl}`
  const fullUrl = `${baseURL}${path}`
  console.log('Generated full URL:', fullUrl)
  
  return fullUrl
}

onMounted(() => {
  loadPhotoGroups()
})
</script>

<style scoped>
.photo-tab {
  padding: 0;
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

.photo-list {
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.photo-list h2 {
  margin-bottom: 1rem;
  color: #333;
}

.photo-table {
  overflow-x: auto;
}

.photo-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.photo-table th,
.photo-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.photo-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.photo-table tr:hover {
  background: #f8f9fa;
}

/* 모달 스타일 */
.photo-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 0.5rem;
  padding: 2rem;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
  width: 1000px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.modal-header h3 {
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #666;
  line-height: 1;
}

.btn-close:hover {
  color: #333;
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

.photo-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
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

.btn-manage {
  background: #17a2b8;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 0.5rem;
}

.btn-manage:hover {
  background: #138496;
}

.btn-upload {
  background: #27ae60;
  color: white;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: 1rem;
}

.btn-upload:hover {
  background: #229954;
}

.btn-delete-small {
  background: #e74c3c;
  color: white;
  padding: 0.25rem 0.75rem;
  border: none;
  border-radius: 0.25rem;
  font-size: 0.875rem;
  cursor: pointer;
  width: 100%;
}

.btn-delete-small:hover {
  background: #c0392b;
}

.photo-form-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.photo-form {
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

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

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
  background: #e74c3c;
  color: white;
}

.btn-delete:hover {
  background: #c0392b;
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
