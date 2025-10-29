<template>
  <div class="content-section">
    <h1 class="section-title">Contact 관리</h1>
    
    <!-- 검색/필터 섹션 -->
    <SearchFilters
      v-model="searchFilters"
      :filters="searchFilterConfig"
      @search="searchContacts"
      @reset="resetFilters"
    />

    <!-- 연락처 목록 -->
    <DataTable
      title="전체 목록"
      :data="contacts"
      :columns="tableColumns"
      :actions="tableActions"
      @action="handleTableAction"
    >
      <template #cell-no="{ index }">
        {{ index + 1 }}
      </template>
    </DataTable>

    <!-- 연락처 등록/수정 폼 -->
    <CrudForm
      title="연락처"
      :fields="formFields"
      v-model="form"
      :editing-item="editingContact"
      @submit="saveContact"
      @cancel="cancelEdit"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { contactService } from '../../services'
import SearchFilters from './common/SearchFilters.vue'
import DataTable from './common/DataTable.vue'
import CrudForm from './common/CrudForm.vue'
import {
  createContactSearchFilters,
  createContactForm,
  resetContactSearchFilters,
  resetContactForm
} from '../../types/dto'

// 검색 필터 설정
const searchFilterConfig = [
  { key: 'name', label: '이름', placeholder: '이름을 입력하세요' },
  { key: 'role', label: '역할', placeholder: '역할을 입력하세요' }
]

// 테이블 컬럼 설정
const tableColumns = [
  { key: 'no', label: 'No' },
  { key: 'name', label: '이름' },
  { key: 'role', label: '역할' },
  { key: 'email', label: '이메일' }
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
      { key: 'name', label: '이름', type: 'text', required: true, placeholder: '이름을 입력하세요' },
      { key: 'role', label: '역할', type: 'text', required: true, placeholder: '역할을 입력하세요' }
    ]
  },
  { key: 'email', label: '이메일', type: 'text', required: true, placeholder: '이메일을 입력하세요' },
  { key: 'displayOrder', label: '표시 순서', type: 'number', min: 0 }
]

// 반응형 데이터
const contacts = ref([])
const searchFilters = ref(createContactSearchFilters())
const form = ref(createContactForm())
const editingContact = ref(null)

// 메서드
const loadContacts = async () => {
  try {
    contacts.value = await contactService.getAllContacts()
  } catch (error) {
    console.error('연락처 로드 실패:', error)
  }
}

const searchContacts = async () => {
  try {
    contacts.value = await contactService.searchContacts(searchFilters.value)
  } catch (error) {
    console.error('연락처 검색 실패:', error)
  }
}

const resetFilters = () => {
  resetContactSearchFilters(searchFilters.value)
  loadContacts()
}

const handleTableAction = (action, item) => {
  switch (action) {
    case 'edit':
      editContact(item)
      break
    case 'delete':
      deleteContact(item.id)
      break
  }
}

const editContact = (contact) => {
  editingContact.value = contact
  form.value = {
    name: contact.name,
    role: contact.role,
    email: contact.email,
    displayOrder: contact.displayOrder || 0
  }
}

const cancelEdit = () => {
  editingContact.value = null
  resetContactForm(form.value)
}

const saveContact = async () => {
  try {
    if (editingContact.value) {
      // 수정
      await contactService.updateContact(editingContact.value.id, form.value)
    } else {
      // 등록
      await contactService.createContact(form.value)
    }
    
    await loadContacts()
    cancelEdit()
  } catch (error) {
    console.error('연락처 저장 실패:', error)
  }
}

const deleteContact = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await contactService.deleteContact(id)
      await loadContacts()
    } catch (error) {
      console.error('연락처 삭제 실패:', error)
    }
  }
}

// Lifecycle
onMounted(() => {
  loadContacts()
})
</script>

<style scoped>
@import './common/admin-common.css';

.content-section {
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #333;
}
</style>
