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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useMediaStore } from '../../../stores'
import { filterData } from '../../../utils'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'

// 스토어 사용
const mediaStore = useMediaStore()

// 검색 필터
const searchFilters = ref({ title: '' })

// Computed properties - 검색 필터 적용
const newsList = computed(() => {
  const allNews = mediaStore.newsItems
  return filterData(allNews, searchFilters.value)
})

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '제목', placeholder: '제목을 입력하세요' }
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

// 메서드
const loadNews = async () => {
  try {
    await mediaStore.loadNews()
  } catch (error) {
    console.error('뉴스 로드 실패:', error)
  }
}

const searchNews = async () => {
  // 검색은 computed로 자동 필터링되므로 별도 처리 불필요
}

const resetFilters = () => {
  searchFilters.value = { title: '' }
}

const handleTableAction = (action, item) => {
  switch (action) {
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
  } catch (error) {
    console.error('뉴스 저장 실패:', error)
    alert('뉴스 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteNews = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await mediaStore.deleteNews(id)
    } catch (error) {
      console.error('뉴스 삭제 실패:', error)
      alert('뉴스 삭제에 실패했습니다: ' + (error.response?.data?.message || error.message))
    }
  }
}

onMounted(() => {
  // 스토어에서 자동으로 로드되므로 별도 로드 불필요
})
</script>

<style scoped>
@import '../common/admin-common.css';

.news-tab {
  padding: 0;
}
</style>
