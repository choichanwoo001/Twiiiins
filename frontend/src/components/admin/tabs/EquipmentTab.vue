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
      :data="equipmentList"
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
      title="장비"
      :fields="formFields"
      v-model="form"
      :editing-item="editingEquipment"
      @submit="saveEquipment"
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
const equipmentList = computed(() => mediaStore.equipmentItems)

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'title', label: '제목', placeholder: '제목을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'no', label: 'No' },
  { key: 'title', label: '제목' }
]

// 테이블 액션 설정
const tableActions = [
  { key: 'edit', label: '수정', class: 'btn-edit' },
  { key: 'delete', label: '삭제', class: 'btn-delete' }
]

// 폼 필드 설정
const formFields = [
  { key: 'title', label: '제목', type: 'text', required: true, placeholder: '제목을 입력하세요' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const searchFilters = ref({ title: '' })
const form = ref({ title: '', displayOrder: 0 })
const editingEquipment = ref(null)

// 메서드
const loadEquipment = async () => {
  try {
    await mediaStore.loadEquipment()
  } catch (error) {
    console.error('장비 로드 실패:', error)
  }
}

const searchEquipment = async () => {
  loadEquipment()
}

const resetFilters = () => {
  searchFilters.value = { title: '' }
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
    title: equipment.title,
    displayOrder: equipment.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingEquipment.value = null
  form.value = { title: '', displayOrder: 0 }
}

const saveEquipment = async () => {
  try {
    if (editingEquipment.value) {
      await mediaStore.updateEquipment(editingEquipment.value.id, form.value)
    } else {
      await mediaStore.addEquipment(form.value)
    }
    
    cancelEdit()
  } catch (error) {
    console.error('장비 저장 실패:', error)
  }
}

const deleteEquipment = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await mediaStore.deleteEquipment(id)
    } catch (error) {
      console.error('장비 삭제 실패:', error)
    }
  }
}

onMounted(() => {
  // 스토어에서 자동으로 로드되므로 별도 로드 불필요
})
</script>

<style scoped>
@import '../common/admin-common.css';

.equipment-tab {
  padding: 0;
}
</style>
