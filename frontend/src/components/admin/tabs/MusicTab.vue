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
      title="음악"
      :fields="formFields"
      v-model="form"
      :editing-item="editingMusic"
      @submit="saveMusic"
      @cancel="cancelEdit"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { musicService } from '../../../services'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'
import {
  createMusicSearchFilters,
  createMusicForm,
  resetMusicSearchFilters,
  resetMusicForm
} from '../../../types/dto'

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
const searchFilters = ref(createMusicSearchFilters())
const form = ref(createMusicForm())
const musicList = ref([])
const editingMusic = ref(null)

// 메서드
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

// Lifecycle
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
