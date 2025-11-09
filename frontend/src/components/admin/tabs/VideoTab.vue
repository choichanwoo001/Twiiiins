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
      :data="displayedVideos"
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
import { ref, computed, onMounted } from 'vue'
import { useMediaStore } from '../../../stores'
import { videoService } from '../../../services'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'

// 스토어 사용
const mediaStore = useMediaStore()

// 검색 필터
const searchFilters = ref({ title: '' })

// 비디오 목록
const isFiltered = ref(false)
const filteredVideos = ref([])

const displayedVideos = computed(() => (
  isFiltered.value ? filteredVideos.value : mediaStore.videos
))

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
const form = ref({ title: '', youtubeUrl: '', displayOrder: 0 })
const editingVideo = ref(null)

// YouTube URL을 Embed URL로 변환하는 함수
const convertToEmbedUrl = (url) => {
  if (!url || typeof url !== 'string') return ''
  
  const trimmedUrl = url.trim()
  if (trimmedUrl === '') return ''
  
  // 이미 embed URL인 경우 쿼리 파라미터 제거 후 반환
  if (trimmedUrl.includes('youtube.com/embed/')) {
    const embedMatch = trimmedUrl.match(/youtube\.com\/embed\/([a-zA-Z0-9_-]+)/)
    if (embedMatch && embedMatch[1]) {
      return `https://www.youtube.com/embed/${embedMatch[1]}`
    }
  }
  
  // YouTube URL에서 video ID 추출
  // 지원 형식:
  // - https://www.youtube.com/watch?v=VIDEO_ID
  // - https://youtu.be/VIDEO_ID
  // - https://youtube.com/watch?v=VIDEO_ID&feature=...
  // - https://youtu.be/VIDEO_ID?si=...
  
  let videoId = null
  
  // youtube.com/watch?v= 형식
  const watchMatch = trimmedUrl.match(/[?&]v=([a-zA-Z0-9_-]+)/)
  if (watchMatch && watchMatch[1]) {
    videoId = watchMatch[1]
  }
  // youtu.be/ 형식
  else if (trimmedUrl.includes('youtu.be/')) {
    const shortMatch = trimmedUrl.match(/youtu\.be\/([a-zA-Z0-9_-]+)/)
    if (shortMatch && shortMatch[1]) {
      videoId = shortMatch[1]
    }
  }
  // youtube.com/embed/ 형식 (다시 한번 확인)
  else if (trimmedUrl.includes('youtube.com/embed/')) {
    const embedMatch = trimmedUrl.match(/youtube\.com\/embed\/([a-zA-Z0-9_-]+)/)
    if (embedMatch && embedMatch[1]) {
      videoId = embedMatch[1]
    }
  }
  
  if (videoId) {
    return `https://www.youtube.com/embed/${videoId}`
  }
  
  // 변환 실패 시 빈 문자열 반환 (에러 방지)
  console.error('YouTube URL 변환 실패:', trimmedUrl)
  return ''
}

// 메서드
const loadVideos = async () => {
  try {
    await mediaStore.loadVideos()
    if (!isFiltered.value) {
      filteredVideos.value = []
    }
  } catch (error) {
    console.error('비디오 로드 실패:', error)
  }
}

const searchVideos = async () => {
  try {
    const results = await videoService.searchVideos(searchFilters.value)
    filteredVideos.value = results
    isFiltered.value = true
  } catch (error) {
    console.error('비디오 검색 실패:', error)
  }
}

const resetFilters = () => {
  searchFilters.value = { title: '' }
  isFiltered.value = false
  filteredVideos.value = []
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
    
    // embedUrl이 비어있으면 에러
    if (!embedUrl) {
      alert('유효한 YouTube URL을 입력해주세요.\n\n지원 형식:\n- https://www.youtube.com/watch?v=VIDEO_ID\n- https://youtu.be/VIDEO_ID\n- https://www.youtube.com/embed/VIDEO_ID')
      return
    }
    
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
    if (isFiltered.value) {
      await searchVideos()
    }
  } catch (error) {
    console.error('비디오 저장 실패:', error)
    alert('비디오 저장에 실패했습니다: ' + (error.response?.data?.message || error.message))
  }
}

const deleteVideo = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await mediaStore.deleteVideo(id)
      if (isFiltered.value) {
        await searchVideos()
      }
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
