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
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'

// 스토어 사용
const mediaStore = useMediaStore()

// Computed properties
const newsList = computed(() => mediaStore.newsItems)

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '제목', placeholder: '제목을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'no', label: 'No' },
  { key: 'title', label: '제목' },
  { key: 'linkUrl', label: '링크 URL' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'title', label: '제목', type: 'text', required: true, placeholder: '제목을 입력하세요' },
  { key: 'linkUrl', label: '링크 URL', type: 'text', required: true, placeholder: '링크 URL을 입력하세요' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const searchFilters = ref({ title: '' })
const form = ref({ title: '', linkUrl: '', displayOrder: 0 })
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
  loadNews()
}

const resetFilters = () => {
  searchFilters.value = { title: '' }
  loadNews()
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
      await mediaStore.updateNews(editingNews.value.id, form.value)
    } else {
      await mediaStore.addNews(form.value)
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
