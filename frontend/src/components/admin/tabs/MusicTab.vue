<template>
  <div class="music-tab">
    <!-- 검색/필터 섹션 -->
    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label>제목</label>
          <input v-model="searchFilters.title" placeholder="제목을 입력하세요" />
        </div>
        <div class="filter-group">
          <label>아티스트</label>
          <input v-model="searchFilters.artist" placeholder="아티스트를 입력하세요" />
        </div>
        <div class="filter-actions">
          <button class="btn-reset" @click="resetFilters">초기화</button>
          <button class="btn-search" @click="searchMusic">검색</button>
        </div>
      </div>
    </div>

    <!-- 음악 목록 -->
    <div class="music-list">
      <h2>전체 목록</h2>
      <div class="music-table">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>제목</th>
              <th>아티스트</th>
              <th>커버 URL</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(music, index) in musicList" :key="music.id">
              <td>{{ index + 1 }}</td>
              <td>{{ music.title }}</td>
              <td>{{ music.artist }}</td>
              <td>{{ music.coverUrl }}</td>
              <td>
                <button class="btn-edit" @click="editMusic(music)">수정</button>
                <button class="btn-delete" @click="deleteMusic(music.id)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 음악 등록/수정 폼 -->
    <div class="music-form-section">
      <h2>{{ editingMusic ? '음악 수정' : '새 음악 등록' }}</h2>
      <form @submit.prevent="saveMusic" class="music-form">
        <div class="form-row">
          <div class="form-group">
            <label>제목 *</label>
            <input v-model="form.title" required />
          </div>
          <div class="form-group">
            <label>아티스트 *</label>
            <input v-model="form.artist" required />
          </div>
        </div>

        <div class="form-group">
          <label>커버 이미지 *</label>
          <div class="file-upload-container">
            <input 
              type="file" 
              ref="fileInput" 
              @change="handleFileUpload" 
              accept="image/*"
              style="display: none"
            />
            <button type="button" class="btn-upload" @click="$refs.fileInput.click()">
              파일 선택
            </button>
            <span v-if="form.coverUrl" class="file-name">{{ form.coverUrl }}</span>
          </div>
        </div>

        <div class="form-group">
          <label>링크 URL</label>
          <input v-model="form.linkUrl" placeholder="Spotify, Apple Music 등" />
        </div>

        <div class="form-group">
          <label>표시 순서</label>
          <input type="number" v-model="form.displayOrder" min="0" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-save">{{ editingMusic ? '수정' : '등록' }}</button>
          <button type="button" class="btn-cancel" @click="cancelEdit" v-if="editingMusic">취소</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { musicService } from '../../../services'
import {
  createMusicSearchFilters,
  createMusicForm,
  resetMusicSearchFilters,
  resetMusicForm
} from '../../../types/dto'

// Reactive data
const searchFilters = ref(createMusicSearchFilters())
const form = ref(createMusicForm())
const musicList = ref([])
const editingMusic = ref(null)

// Methods
const loadMusic = async () => {
  try {
    musicList.value = await musicService.getAllMusic()
  } catch (error) {
    console.error('음악 로드 실패:', error)
  }
}

const searchMusic = async () => {
  try {
    musicList.value = await musicService.searchMusic(searchFilters.value)
  } catch (error) {
    console.error('음악 검색 실패:', error)
  }
}

const resetFilters = () => {
  resetMusicSearchFilters(searchFilters.value)
  loadMusic()
}

const editMusic = (music) => {
  editingMusic.value = music
  form.value = {
    title: music.title,
    artist: music.artist,
    coverUrl: music.coverUrl || '',
    linkUrl: music.linkUrl || '',
    displayOrder: music.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingMusic.value = null
  resetMusicForm(form.value)
}

const saveMusic = async () => {
  try {
    if (editingMusic.value) {
      // 수정
      await musicService.updateMusic(editingMusic.value.id, form.value)
    } else {
      // 등록
      await musicService.createMusic(form.value)
    }
    
    await loadMusic()
    cancelEdit()
  } catch (error) {
    console.error('음악 저장 실패:', error)
  }
}

const deleteMusic = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await musicService.deleteMusic(id)
      await loadMusic()
    } catch (error) {
      console.error('음악 삭제 실패:', error)
    }
  }
}

const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 파일 업로드 로직 (실제 구현 필요)
    form.value.coverUrl = file.name
  }
}

// Lifecycle
onMounted(() => {
  loadMusic()
})
</script>

<style scoped>
.music-tab {
  padding: 0;
}

.search-section {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 0.5rem;
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

.music-list {
  margin-bottom: 2rem;
}

.music-list h2 {
  margin-bottom: 1rem;
  color: #333;
}

.music-table {
  overflow-x: auto;
}

.music-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.music-table th,
.music-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.music-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.music-table tr:hover {
  background: #f8f9fa;
}

.music-form-section {
  background: #f8f9fa;
  padding: 2rem;
  border-radius: 0.5rem;
}

.music-form {
  max-width: 50rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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
