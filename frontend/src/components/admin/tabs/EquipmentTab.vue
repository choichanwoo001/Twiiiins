<template>
  <div class="equipment-tab">
    <!-- 검색/필터 섹션 -->
    <SearchFilters
      v-model="searchFilters"
      :filters="searchFilterConfig"
      @search="searchEquipment"
      @reset="resetFilters"
    />

    <!-- 장비 목록 -->
    <DataTable
      title="전체 목록"
      :data="displayedEquipment"
      :columns="tableColumns"
      :actions="tableActions"
      @action="handleTableAction"
    >
      <template #cell-no="{ index }">
        {{ index + 1 }}
      </template>
    </DataTable>

    <!-- 장비 등록/수정 폼 -->
    <CrudForm
      ref="crudFormRef"
      title="장비"
      :fields="formFields"
      v-model="form"
      :editing-item="editingEquipment"
      @submit="saveEquipment"
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
import { ref, computed, onMounted } from 'vue'
import axios from '../../../api/axios'
import { useMediaStore } from '../../../stores'
import { equipmentService } from '../../../services'
import { logError, getErrorMessage } from '../../../utils/errorHandler'
import { ConfirmDialog, AlertDialog } from '../../common'
import SearchFilters from '../common/SearchFilters.vue'
import DataTable from '../common/DataTable.vue'
import CrudForm from '../common/CrudForm.vue'

// 스토어 사용
const mediaStore = useMediaStore()

// 검색 필터
const searchFilters = ref({ name: '' })

// 장비 목록
const isFiltered = ref(false)
const filteredEquipment = ref([])

const displayedEquipment = computed(() => (
  isFiltered.value ? filteredEquipment.value : mediaStore.equipmentItems
))

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'name', label: '장비명', placeholder: '장비명을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'no', label: 'No' },
  { key: 'name', label: '장비명' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'name', label: '장비명', type: 'text', required: true, placeholder: '장비명을 입력하세요' },
  { key: 'imageUrl', label: '이미지', type: 'file', required: true, accept: 'image/*' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const form = ref({ name: '', imageUrl: '', displayOrder: 0 })
const editingEquipment = ref(null)
const crudFormRef = ref(null)

// 다이얼로그 상태 및 헬퍼 함수 (ConcertAdmin.vue와 동일한 패턴)
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
const loadEquipment = async () => {
  try {
    await mediaStore.loadEquipment()
    if (!isFiltered.value) {
      filteredEquipment.value = []
    }
  } catch (error) {
    logError(error, '장비 로드')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const searchEquipment = async () => {
  try {
    const results = await equipmentService.searchEquipment(searchFilters.value)
    filteredEquipment.value = results
    isFiltered.value = true
  } catch (error) {
    logError(error, '장비 검색')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const resetFilters = () => {
  searchFilters.value = { name: '' }
  isFiltered.value = false
  filteredEquipment.value = []
  loadEquipment()
}

const handleTableAction = (action, item) => {
  switch (action) {
    case 'edit':
      editEquipment(item)
      break
    case 'delete':
      deleteEquipment(item.id)
      break
  }
}

const editEquipment = (equipment) => {
  editingEquipment.value = equipment
  form.value = {
    name: equipment.name || '',
    imageUrl: equipment.imageUrl || '',
    displayOrder: equipment.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingEquipment.value = null
  form.value = { name: '', imageUrl: '', displayOrder: 0 }
}

const saveEquipment = async () => {
  try {
    // 이미지가 선택된 경우 먼저 업로드
    const fileObject = crudFormRef.value?.getFileObject('imageUrl')
    
    if (fileObject) {
      // FormData로 파일 업로드
      const formData = new FormData()
      formData.append('file', fileObject)
      
      // 파일 업로드 API 호출
      const uploadResponse = await axios.post('/upload/image', formData)
      
      // 업로드된 파일의 S3 URL 저장
      if (uploadResponse.data && uploadResponse.data.url) {
        form.value.imageUrl = uploadResponse.data.url
      } else if (uploadResponse.data && uploadResponse.data.data && uploadResponse.data.data.url) {
        form.value.imageUrl = uploadResponse.data.data.url
      }
      
      // 파일 객체 제거
      crudFormRef.value?.clearFileObject('imageUrl')
    }
    
    // 장비 정보 저장
    if (editingEquipment.value) {
      await mediaStore.updateEquipment(editingEquipment.value.id, form.value)
    } else {
      await mediaStore.addEquipment(form.value)
    }
    
    cancelEdit()
    if (isFiltered.value) {
      await searchEquipment()
    }
  } catch (error) {
    logError(error, '장비 저장')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

const deleteEquipment = async (id) => {
  try {
    const confirmed = await showConfirm('정말 삭제하시겠습니까?', '삭제 확인')
    if (confirmed) {
      await mediaStore.deleteEquipment(id)
      if (isFiltered.value) {
        await searchEquipment()
      }
    }
  } catch (error) {
    logError(error, '장비 삭제')
    await showAlert(getErrorMessage(error), '오류', 'danger')
  }
}

onMounted(() => {
  loadEquipment()
})

</script>

<style scoped>
@import '../common/admin-common.css';

.equipment-tab {
  padding: 0;
}
</style>
