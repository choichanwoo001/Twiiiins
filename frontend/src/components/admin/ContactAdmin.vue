<template>
  <div class="content-section">
    <h1 class="section-title">Contact 관리</h1>
    
    <!-- 검색/필터 섹션 -->
    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label>이름</label>
          <input v-model="searchFilters.name" placeholder="이름을 입력하세요" />
        </div>
        <div class="filter-group">
          <label>역할</label>
          <input v-model="searchFilters.role" placeholder="역할을 입력하세요" />
        </div>
        <div class="filter-actions">
          <button class="btn-reset" @click="resetFilters">초기화</button>
          <button class="btn-search" @click="searchContacts">검색</button>
        </div>
      </div>
    </div>

    <!-- 연락처 목록 -->
    <div class="contacts-list">
      <h2>전체 목록</h2>
      <div class="contacts-table">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>이름</th>
              <th>역할</th>
              <th>이메일</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(contact, index) in contacts" :key="contact.id">
              <td>{{ index + 1 }}</td>
              <td>{{ contact.name }}</td>
              <td>{{ contact.role }}</td>
              <td>{{ contact.email }}</td>
              <td>
                <button class="btn-edit" @click="editContact(contact)">수정</button>
                <button class="btn-delete" @click="deleteContact(contact.id)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 연락처 등록/수정 폼 -->
    <div class="contact-form-section">
      <h2>{{ editingContact ? '연락처 수정' : '새 연락처 등록' }}</h2>
      <form @submit.prevent="saveContact" class="contact-form">
        <div class="form-row">
          <div class="form-group">
            <label>이름 *</label>
            <input v-model="form.name" required />
          </div>
          <div class="form-group">
            <label>역할 *</label>
            <input v-model="form.role" required />
          </div>
        </div>

        <div class="form-group">
          <label>이메일 *</label>
          <input type="email" v-model="form.email" required />
        </div>

        <div class="form-group">
          <label>표시 순서</label>
          <input type="number" v-model="form.displayOrder" min="0" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-save">{{ editingContact ? '수정' : '등록' }}</button>
          <button type="button" class="btn-cancel" @click="cancelEdit" v-if="editingContact">취소</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { contactService } from '../../services'
import {
  createContactSearchFilters,
  createContactForm,
  resetContactSearchFilters,
  resetContactForm
} from '../../types/dto'

// Props
const props = defineProps({
  contacts: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:contacts'])

// Reactive data
const searchFilters = ref(createContactSearchFilters())
const form = ref(createContactForm())
const editingContact = ref(null)

// Methods
const loadContacts = async () => {
  try {
    const contacts = await contactService.getAllContacts()
    emit('update:contacts', contacts)
  } catch (error) {
    console.error('연락처 로드 실패:', error)
  }
}

const searchContacts = async () => {
  try {
    const contacts = await contactService.searchContacts(searchFilters.value)
    emit('update:contacts', contacts)
  } catch (error) {
    console.error('연락처 검색 실패:', error)
  }
}

const resetFilters = () => {
  resetContactSearchFilters(searchFilters.value)
  loadContacts()
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
.content-section {
  padding: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #333;
}

.search-section {
  background: white;
  padding: 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.search-filters {
  display: flex;
  gap: 1rem;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: #555;
}

.filter-group input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  font-size: 0.9rem;
}

.filter-actions {
  display: flex;
  gap: 0.5rem;
}

.contacts-list {
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.contacts-list h2 {
  margin-bottom: 1rem;
  color: #333;
}

.contacts-table {
  overflow-x: auto;
}

.contacts-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.contacts-table th,
.contacts-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.contacts-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.contacts-table tr:hover {
  background: #f8f9fa;
}

.contact-form-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.contact-form {
  max-width: 50rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.form-group label {
  font-weight: 500;
  color: #555;
}

.form-group input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  font-size: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

/* 버튼 스타일 */
.btn-reset, .btn-search {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset {
  background: #95a5a6;
  color: white;
}

.btn-reset:hover {
  background: #7f8c8d;
}

.btn-search {
  background: #3498db;
  color: white;
}

.btn-search:hover {
  background: #2980b9;
}

.btn-edit, .btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 0.5rem;
}

.btn-edit {
  background: #f39c12;
  color: white;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-delete {
  background: #943C31;
  color: white;
}

.btn-delete:hover {
  background: #7a2f26;
}

.btn-save {
  padding: 0.75rem 2rem;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover {
  background: #229954;
}

.btn-cancel {
  padding: 0.75rem 2rem;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #7f8c8d;
}
</style>
