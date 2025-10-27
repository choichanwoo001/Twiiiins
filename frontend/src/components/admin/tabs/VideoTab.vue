<template>
  <div class="video-tab">
    <!-- 검색/필터 섹션 -->
    <SearchFilters
      v-model="searchFilters"
      :filters="searchFilterConfig"
      @search="searchVideos"
      @reset="resetFilters"
    />

    <!-- 비디오 목록 -->
    <DataTable
      title="전체 목록"
      :data="videoList"
      :columns="tableColumns"
      :actions="tableActions"
      @action="handleTableAction"
    />

    <!-- 비디오 등록/수정 폼 -->
    <CrudForm
      title="비디오"
      :fields="formFields"
      v-model="form"
      :editing-item="editingVideo"
      @submit="saveVideo"
      @cancel="cancelEdit"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { videoService } from '../../../services'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '제목', placeholder: '제목을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'title', label: '제목' },
  { key: 'youtubeUrl', label: 'YouTube URL' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'title', label: '제목', type: 'text', required: true, placeholder: '제목을 입력하세요' },
  { key: 'youtubeUrl', label: 'YouTube URL', type: 'text', required: true, placeholder: 'YouTube URL을 입력하세요' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const searchFilters = ref({ title: '' })
const form = ref({ title: '', youtubeUrl: '', displayOrder: 0 })
const videoList = ref([])
const editingVideo = ref(null)

// 메서드
const loadVideos = async () => {
  try {
    videoList.value = await videoService.getAllVideos()
  } catch (error) {
    console.error('비디오 로드 실패:', error)
  }
}

const searchVideos = async () => {
  try {
    videoList.value = await videoService.searchVideos(searchFilters.value)
  } catch (error) {
    console.error('비디오 검색 실패:', error)
  }
}

const resetFilters = () => {
  searchFilters.value = { title: '' }
  loadVideos()
}

const handleTableAction = (action, item) => {
  switch (action) {
    case 'edit':
      editVideo(item)
      break
    case 'delete':
      deleteVideo(item.id)
      break
  }
}

const editVideo = (video) => {
  editingVideo.value = video
  form.value = {
    title: video.title,
    youtubeUrl: video.youtubeUrl,
    displayOrder: video.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingVideo.value = null
  form.value = { title: '', youtubeUrl: '', displayOrder: 0 }
}

const saveVideo = async () => {
  try {
    if (editingVideo.value) {
      await videoService.updateVideo(editingVideo.value.id, form.value)
    } else {
      await videoService.createVideo(form.value)
    }
    
    await loadVideos()
    cancelEdit()
  } catch (error) {
    console.error('비디오 저장 실패:', error)
  }
}

const deleteVideo = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await videoService.deleteVideo(id)
      await loadVideos()
    } catch (error) {
      console.error('비디오 삭제 실패:', error)
    }
  }
}

onMounted(() => {
  loadVideos()
})
</script>

<style scoped>
@import '../common/admin-common.css';

.video-tab {
  padding: 0;
}
</style>
