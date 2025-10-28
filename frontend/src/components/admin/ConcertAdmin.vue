<template>
  <div class="content-section">
    <h1 class="section-title">Concerts 관리</h1>
    
    <!-- 검색/필터 섹션 -->
    <div class="search-section">
      <div class="search-filters">
        <div class="filter-group">
          <label>콘서트명</label>
          <input v-model="searchFilters.name" placeholder="콘서트명을 입력하세요" />
        </div>
        <div class="filter-group">
          <label>장소</label>
          <input v-model="searchFilters.location" placeholder="장소를 입력하세요" />
        </div>
        <div class="filter-group">
          <label>날짜 범위</label>
          <div class="date-range">
            <input type="date" v-model="searchFilters.startDate" />
            <span>~</span>
            <input type="date" v-model="searchFilters.endDate" />
          </div>
        </div>
        <div class="filter-actions">
          <BaseButton variant="secondary" @click="resetFilters">초기화</BaseButton>
          <BaseButton variant="primary" @click="searchConcerts">검색</BaseButton>
        </div>
      </div>
    </div>

    <!-- 콘서트 목록 -->
    <div class="concerts-list">
      <div class="concerts-header">
        <h2>전체 목록</h2>
        <BaseButton variant="warning" @click="triggerAutoMove">
          자동 이동 실행
        </BaseButton>
      </div>
      <div class="concerts-table">
        <table>
          <thead>
            <tr>
              <th>No</th>
              <th>콘서트</th>
              <th>장소</th>
              <th>날짜</th>
              <th>상태</th>
              <th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(concert, index) in concerts" :key="concert.id">
              <td>{{ index + 1 }}</td>
              <td>{{ concert.name }}</td>
              <td>{{ concert.location }}</td>
              <td>{{ formatDate(concert.date) }}</td>
              <td>
                <span class="status-badge" :class="{ past: concert.isPast }">
                  {{ concert.isPast ? '지난 공연' : '예정' }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <BaseButton size="small" variant="secondary" @click="editConcert(concert)">수정</BaseButton>
                  <BaseButton size="small" variant="danger" @click="deleteConcert(concert.id)">삭제</BaseButton>
                  <BaseButton 
                    v-if="!concert.isPast" 
                    size="small"
                    variant="warning" 
                    @click="moveToPastEvent(concert.id)"
                  >
                    Past Event로 이동
                  </BaseButton>
                  <BaseButton 
                    v-if="concert.isPast" 
                    size="small"
                    variant="success" 
                    @click="moveToUpcomingEvent(concert.id)"
                  >
                    Upcoming으로 이동
                  </BaseButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 콘서트 등록/수정 폼 -->
    <div class="concert-form-section">
      <h2>{{ editingConcert ? '콘서트 수정' : '새 콘서트 등록' }}</h2>
      <form @submit.prevent="saveConcert" class="concert-form">
        <div class="form-row">
          <div class="form-group">
            <label>날짜 *</label>
            <input type="date" v-model="form.date" required />
          </div>
          <div class="form-group">
            <label>장소 *</label>
            <input v-model="form.location" required />
          </div>
        </div>

        <div class="form-group">
          <label>이름 *</label>
          <input v-model="form.name" required />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>시작 시간</label>
            <input v-model="form.startTime" />
          </div>
          <div class="form-group">
            <label>티켓 정보 URL</label>
            <input v-model="form.ticketInfo" />
          </div>
        </div>

        <div class="form-group">
          <label>전체 주소</label>
          <input v-model="form.fullLocation" />
        </div>

        <div class="form-group">
          <label>구글맵 URL</label>
          <input v-model="form.googleMapUrl" />
        </div>

        <div class="form-group">
          <label>협업 정보</label>
          <input v-model="form.collaborationInfo" />
        </div>

        <div class="form-group">
          <label>
            <input type="checkbox" v-model="form.isPast" />
            지난 공연
          </label>
        </div>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary">{{ editingConcert ? '수정' : '등록' }}</BaseButton>
          <BaseButton type="button" variant="secondary" @click="cancelEdit" v-if="editingConcert">취소</BaseButton>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useConcertStore } from '../../stores'
import { BaseButton } from '../common'
import { formatDate } from '../../utils/commonHelpers'
import {
  createConcertSearchFilters,
  createConcertForm,
  resetConcertSearchFilters,
  resetConcertForm
} from '../../types/dto'

// 스토어 사용
const concertStore = useConcertStore()

// Computed properties
const concerts = computed(() => {
  return [...concertStore.concerts].sort((a, b) => {
    // 날짜를 Date 객체로 변환하여 비교
    const dateA = new Date(a.date)
    const dateB = new Date(b.date)
    
    // 오름차순 정렬 (가장 이른 날짜가 위로)
    return dateA - dateB
  })
})

// Reactive data
const searchFilters = ref(createConcertSearchFilters())
const form = ref(createConcertForm())
const editingConcert = ref(null)

// Methods
const loadConcerts = async () => {
  try {
    await concertStore.loadConcerts()
  } catch (error) {
    console.error('콘서트 로드 실패:', error)
  }
}

const searchConcerts = async () => {
  try {
    await concertStore.loadConcerts() // 스토어에서 검색 기능이 있다면 사용, 없으면 전체 로드
  } catch (error) {
    console.error('콘서트 검색 실패:', error)
  }
}

const resetFilters = () => {
  resetConcertSearchFilters(searchFilters.value)
  loadConcerts()
}

const editConcert = (concert) => {
  editingConcert.value = concert
  form.value = {
    date: concert.date,
    location: concert.location,
    name: concert.name,
    startTime: concert.startTime || '',
    ticketInfo: concert.ticketInfo || '',
    fullLocation: concert.fullLocation || '',
    googleMapUrl: concert.googleMapUrl || '',
    collaborationInfo: concert.collaborationInfo || '',
    isPast: concert.isPast
  }
}

const cancelEdit = () => {
  editingConcert.value = null
  resetConcertForm(form.value)
}

const saveConcert = async () => {
  try {
    if (editingConcert.value) {
      // 수정
      await concertStore.updateConcert(editingConcert.value.id, form.value)
    } else {
      // 등록
      await concertStore.addConcert(form.value)
    }
    
    cancelEdit()
  } catch (error) {
    console.error('콘서트 저장 실패:', error)
  }
}

const deleteConcert = async (id) => {
  if (confirm('정말 삭제하시겠습니까?')) {
    try {
      await concertStore.deleteConcert(id)
    } catch (error) {
      console.error('콘서트 삭제 실패:', error)
    }
  }
}

const moveToPastEvent = async (id) => {
  try {
    await concertStore.moveToPastEvent(id)
  } catch (error) {
    console.error('Past Event 이동 실패:', error)
  }
}

const moveToUpcomingEvent = async (id) => {
  try {
    await concertStore.moveToUpcomingEvent(id)
  } catch (error) {
    console.error('Upcoming 이동 실패:', error)
  }
}

const triggerAutoMove = async () => {
  try {
    await concertStore.triggerAutoMove()
  } catch (error) {
    console.error('자동 이동 실행 실패:', error)
  }
}

// Lifecycle
onMounted(() => {
  // 스토어에서 자동으로 로드되므로 별도 로드 불필요
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

.date-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.concerts-list {
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.concerts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.concerts-table {
  overflow-x: auto;
}

.concerts-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.concerts-table th,
.concerts-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.concerts-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.concerts-table tr:hover {
  background: #f8f9fa;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.8rem;
  font-weight: 500;
  background: #28a745;
  color: white;
}

.status-badge.past {
  background: #6c757d;
}

.concert-form-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.concert-form {
  max-width: 50rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
}
</style>
