<template>
  <div class="news-tab">
    <!-- 검색/필터 섹션 -->
    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label>제목</label>
          <input v-model="searchFilters.title" placeholder="제목을 입력하세요" />
        </div>
        <div class="filter-actions">
          <button class="btn-reset" @click="resetFilters">초기화</button>
          <button class="btn-search" @click="searchNews">검색</button>
        </div>
      </div>
    </div>

    <!-- 뉴스 목록 -->
    <div class="news-list">
      <h2>전체 목록</h2>
      <div class="news-table">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>제목</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(news, index) in newsList" :key="news.id">
              <td>{{ index + 1 }}</td>
              <td>{{ news.title }}</td>
              <td>
                <button class="btn-edit" @click="editNews(news)">수정</button>
                <button class="btn-delete" @click="deleteNews(news.id)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 뉴스 등록/수정 폼 -->
    <div class="news-form-section">
      <h2>{{ editingNews ? '뉴스 수정' : '새 뉴스 등록' }}</h2>
      <form @submit.prevent="saveNews" class="news-form">
        <div class="form-group">
          <label>제목 *</label>
          <input v-model="form.title" required />
        </div>

        <div class="form-group">
          <label>링크 URL *</label>
          <input v-model="form.linkUrl" required />
        </div>

        <div class="form-group">
          <label>표시 순서</label>
          <input type="number" v-model="form.displayOrder" min="0" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-save">{{ editingNews ? '수정' : '등록' }}</button>
          <button type="button" class="btn-cancel" @click="cancelEdit" v-if="editingNews">취소</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../../../api/axios'

const searchFilters = ref({ title: '' })
const form = ref({ title: '', linkUrl: '', displayOrder: 0 })
const newsList = ref([])
const editingNews = ref(null)

const loadNews = async () => {
  try {
    const res = await axios.get('/api/media/news')
    newsList.value = res.data
  } catch (error) {
    console.error('뉴스 로드 실패:', error)
  }
}

const searchNews = async () => {
  loadNews()
}

const resetFilters = () => {
  searchFilters.value = { title: '' }
  loadNews()
}

const editNews = (news) => {
  editingNews.value = news
  form.value = {
    title: news.title,
    linkUrl: news.linkUrl,
    displayOrder: news.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingNews.value = null
  form.value = { title: '', linkUrl: '', displayOrder: 0 }
}

const saveNews = async () => {
  try {
    if (editingNews.value) {
      await axios.put(`/api/media/news/${editingNews.value.id}`, form.value)
    } else {
      await axios.post('/api/media/news', form.value)
    }
    
    await loadNews()
    cancelEdit()
  } catch (error) {
    console.error('뉴스 저장 실패:', error)
  }
}

const deleteNews = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await axios.delete(`/api/media/news/${id}`)
      await loadNews()
    } catch (error) {
      console.error('뉴스 삭제 실패:', error)
    }
  }
}

onMounted(() => {
  loadNews()
})
</script>

<style scoped>
.news-tab {
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

.news-list {
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.news-list h2 {
  margin-bottom: 1rem;
  color: #333;
}

.news-table {
  overflow-x: auto;
}

.news-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.news-table th,
.news-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.news-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.news-table tr:hover {
  background: #f8f9fa;
}

.news-form-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.news-form {
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
