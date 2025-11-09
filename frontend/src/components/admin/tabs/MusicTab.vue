<template>
  <div class="music-tab">
    <!-- 검색/필터 섹션 -->
    <SearchFilters
      v-model="searchFilters"
      :filters="searchFilterConfig"
      @search="searchMusic"
      @reset="resetFilters"
    />

    <!-- 음악 목록 -->
    <DataTable
      title="전체 목록"
      :data="musicList"
      :columns="tableColumns"
      :actions="tableActions"
      @action="handleTableAction"
    />

    <!-- 음악 등록/수정 폼 -->
    <CrudForm
      ref="crudFormRef"
      title="음악"
      :fields="formFields"
      v-model="form"
      :editing-item="editingMusic"
      @submit="saveMusic"
      @cancel="cancelEdit"
    />

    <!-- 공통 다이얼로그 -->
    <ConfirmDialog
      :is-visible="confirmDialog.isVisible"
      :title="confirmDialog.title"
      :message="confirmDialog.message"
      :confirm-text="confirmDialog.confirmText"
      :cancel-text="confirmDialog.cancelText"
      :confirm-variant="confirmDialog.confirmVariant"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />

    <AlertDialog
      :is-visible="alertDialog.isVisible"
      :title="alertDialog.title"
      :message="alertDialog.message"
      :button-text="alertDialog.buttonText"
      :button-variant="alertDialog.buttonVariant"
      @close="handleAlertClose"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMediaStore } from '../../../stores'
import { musicService } from '../../../services'
import { logError, getErrorMessage } from '../../../utils/errorHandler'
import { ConfirmDialog, AlertDialog } from '../../common'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'
import {
  createMusicSearchFilters,
  createMusicForm,
  resetMusicSearchFilters,
  resetMusicForm
} from '../../../types/dto'

// 스토어 사용
const mediaStore = useMediaStore()

// 검색 필터
const searchFilters = ref(createMusicSearchFilters())

// 음악 목록
const musicList = ref([])

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '제목', placeholder: '제목을 입력하세요' },
  { key: 'artist', label: '아티스트', placeholder: '아티스트를 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'title', label: '제목' },
  { key: 'artist', label: '아티스트' },
  { key: 'coverUrl', label: '커버 URL' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  {
    row: true,
    fields: [
      { key: 'title', label: '제목', type: 'text', required: true, placeholder: '제목을 입력하세요' },
      { key: 'artist', label: '아티스트', type: 'text', required: true, placeholder: '아티스트를 입력하세요' }
    ]
  },
  { key: 'coverUrl', label: '커버 이미지', type: 'file', required: true, accept: 'image/*' },
  { key: 'linkUrl', label: '링크 URL', type: 'text', placeholder: 'Spotify, Apple Music 등' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const form = ref(createMusicForm())
const editingMusic = ref(null)
const crudFormRef = ref(null)

// 다이얼로그 상태 및 헬퍼 함수
const confirmDialog = ref({
  isVisible: false,
  title: '확인',
  message: '',
  confirmText: '확인',
  cancelText: '취소',
  confirmVariant: 'danger',
  resolve: null
})

const alertDialog = ref({
  isVisible: false,
  title: '알림',
  message: '',
  buttonText: '확인',
  buttonVariant: 'primary',
  resolve: null
})

const showConfirm = (message, title = '확인') => {
  return new Promise((resolve) => {
    confirmDialog.value = {
      isVisible: true,
      title,
      message,
      confirmText: '확인',
      cancelText: '취소',
      confirmVariant: 'danger',
      resolve
    }
  })
}

const showAlert = (message, title = '알림', variant = 'primary') => {
  return new Promise((resolve) => {
    alertDialog.value = {
      isVisible: true,
      title,
      message,
      buttonText: '확인',
      buttonVariant: variant,
      resolve
    }
  })
}

const handleConfirm = () => {
  if (confirmDialog.value.resolve) {
    confirmDialog.value.resolve(true)
  }
  confirmDialog.value.isVisible = false
}

const handleCancel = () => {
  if (confirmDialog.value.resolve) {
    confirmDialog.value.resolve(false)
  }
  confirmDialog.value.isVisible = false
}

const handleAlertClose = () => {
  if (alertDialog.value.resolve) {
    alertDialog.value.resolve()
  }
  alertDialog.value.isVisible = false
}

// 메서드
const loadMusic = async () => {
  try {
    await mediaStore.loadMusic()
    musicList.value = mediaStore.musicItems
  } catch (error) {
    logError(error, '음악 로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const searchMusic = async () => {
  try {
    const results = await musicService.searchMusic(searchFilters.value)
    musicList.value = results
  } catch (error) {
    logError(error, '음악 검색')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const resetFilters = () => {
  resetMusicSearchFilters(searchFilters.value)
  loadMusic()
}

const handleTableAction = (action, item) => {
  switch (action) {
    case 'edit':
      editMusic(item)
      break
    case 'delete':
      deleteMusic(item.id)
      break
  }
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
    // 커버 이미지가 선택된 경우 먼저 업로드
    const fileObject = crudFormRef.value?.getFileObject('coverUrl')
    
    if (fileObject) {
      // FormData로 파일 업로드
      const formData = new FormData()
      formData.append('file', fileObject)
      
      // 파일 업로드 API 호출
      const uploadResponse = await axios.post('/upload/image', formData)
      
      // 업로드된 파일의 S3 URL 저장
      if (uploadResponse.data && uploadResponse.data.url) {
        form.value.coverUrl = uploadResponse.data.url
      } else if (uploadResponse.data && uploadResponse.data.data && uploadResponse.data.data.url) {
        form.value.coverUrl = uploadResponse.data.data.url
      }
      
      // 파일 객체 제거
      crudFormRef.value?.clearFileObject('coverUrl')
    }
    
    // 음악 정보 저장
    if (editingMusic.value) {
      // 수정
      await mediaStore.updateMusic(editingMusic.value.id, form.value)
    } else {
      // 등록
      await mediaStore.addMusic(form.value)
    }
    
    cancelEdit()
    await loadMusic()
  } catch (error) {
    logError(error, '음악 저장')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const deleteMusic = async (id) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')
    if (confirmed) {
      await mediaStore.deleteMusic(id)
      await loadMusic()
    }
  } catch (error) {
    logError(error, '음악 삭제')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

onMounted(() => {
  loadMusic()
})

</script>

<style scoped>
@import '../common/admin-common.css';

.music-tab {
  padding: 0;
}
</style>
