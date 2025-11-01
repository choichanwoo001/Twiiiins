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
import { ref, onMounted, computed } from 'vue'
import { useMediaStore } from '../../../stores'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'

// 스토어 사용
const mediaStore = useMediaStore()

// Computed properties
const videoList = computed(() => mediaStore.videos)

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '제목', placeholder: '제목을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'title', label: '제목' },
  { key: 'embedUrl', label: 'Embed URL' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'title', label: '제목', type: 'text', required: true, placeholder: '제목을 입력하세요' },
  { key: 'youtubeUrl', label: 'YouTube URL', type: 'text', required: true, placeholder: 'YouTube URL 또는 Embed URL을 입력하세요' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const searchFilters = ref({ title: '' })
const form = ref({ title: '', youtubeUrl: '', displayOrder: 0 })
const editingVideo = ref(null)

// YouTube URL을 Embed URL로 변환하는 함수
const convertToEmbedUrl = (url) => {
  if (!url) return ''
  
  // 이미 embed URL인 경우 그대로 반환
  if (url.includes('youtube.com/embed/')) {
    return url.split('?')[0] // 쿼리 파라미터 제거
  }
  
  // YouTube URL에서 video ID 추출
  // 형식: https://www.youtube.com/watch?v=VIDEO_ID
  // 또는: https://youtu.be/VIDEO_ID
  let videoId = null
  
  if (url.includes('youtube.com/watch?v=')) {
    videoId = url.split('watch?v=')[1]?.split('&')[0]
  } else if (url.includes('youtu.be/')) {
    videoId = url.split('youtu.be/')[1]?.split('?')[0]
  } else if (url.includes('youtube.com/embed/')) {
    videoId = url.split('embed/')[1]?.split('?')[0]
  }
  
  if (videoId) {
    return `https://www.youtube.com/embed/${videoId}`
  }
  
  // 변환 실패 시 원본 반환
  return url
}

// 메서드
const loadVideos = async () => {
  try {
    await mediaStore.loadVideos()
  } catch (error) {
    console.error('비디오 로드 실패:', error)
  }
}

const searchVideos = async () => {
  try {
    await mediaStore.loadVideos() // 스토어에서 검색 기능이 있다면 사용
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
    youtubeUrl: video.embedUrl || '', // embedUrl을 youtubeUrl 필드에 표시
    displayOrder: video.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingVideo.value = null
  form.value = { title: '', youtubeUrl: '', displayOrder: 0 }
}

const saveVideo = async () => {
  try {
    // YouTube URL을 Embed URL로 변환
    const embedUrl = convertToEmbedUrl(form.value.youtubeUrl)
    
    // embedUrl 필드로 변환하여 저장
    const videoData = {
      title: form.value.title,
      embedUrl: embedUrl,
      displayOrder: form.value.displayOrder
    }
    
    if (editingVideo.value) {
      await mediaStore.updateVideo(editingVideo.value.id, videoData)
    } else {
      await mediaStore.addVideo(videoData)
    }
    
    cancelEdit()
  } catch (error) {
    console.error('비디오 저장 실패:', error)
    alert('비디오 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteVideo = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await mediaStore.deleteVideo(id)
    } catch (error) {
      console.error('비디오 삭제 실패:', error)
    }
  }
}

onMounted(() => {
  // 스토어에서 자동으로 로드되므로 별도 로드 불필요
})
</script>

<style scoped>
@import '../common/admin-common.css';

.video-tab {
  padding: 0;
}
</style>
