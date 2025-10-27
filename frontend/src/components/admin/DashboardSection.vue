<template>
  <div class="dashboard-section">
    <h1 class="section-title">Dashboard</h1>
    
    <!-- 통계 카드들 -->
    <div class="dashboard-stats">
      <div class="stat-card">
        <div class="stat-icon">🎵</div>
        <div class="stat-content">
          <h3>콘서트</h3>
          <p class="stat-number">{{ concerts.length }}</p>
          <p class="stat-label">총 콘서트 수</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📸</div>
        <div class="stat-content">
          <h3>사진 그룹</h3>
          <p class="stat-number">{{ photoGroups.length }}</p>
          <p class="stat-label">총 사진 그룹 수</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🎬</div>
        <div class="stat-content">
          <h3>미디어</h3>
          <p class="stat-number">{{ totalMediaCount }}</p>
          <p class="stat-label">총 미디어 수</p>
        </div>
      </div>
    </div>

    <!-- 최근 활동 -->
    <div class="dashboard-section">
      <h2>최근 활동</h2>
      <div class="activity-list">
        <div class="activity-item">
          <div class="activity-icon">🎵</div>
          <div class="activity-content">
            <h4>콘서트 관리</h4>
            <p>예정된 콘서트: {{ upcomingConcertsCount }}개</p>
            <p>지난 콘서트: {{ pastConcertsCount }}개</p>
          </div>
        </div>
        
        <div class="activity-item">
          <div class="activity-icon">📸</div>
          <div class="activity-content">
            <h4>사진 관리</h4>
            <p>총 사진 그룹: {{ photoGroups.length }}개</p>
            <p>총 사진 수: {{ totalPhotosCount }}개</p>
          </div>
        </div>
        
        <div class="activity-item">
          <div class="activity-icon">🎬</div>
          <div class="activity-content">
            <h4>미디어 관리</h4>
            <p>음악: {{ musicList.length }}개</p>
            <p>비디오: {{ videos.length }}개</p>
            <p>사진 그룹: {{ photoGroups.length }}개</p>
            <p>뉴스: {{ newsList.length }}개</p>
            <p>장비: {{ equipmentList.length }}개</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 빠른 작업 -->
    <div class="dashboard-section">
      <h2>빠른 작업</h2>
      <div class="quick-actions">
        <button 
          class="quick-action-btn" 
          @click="$emit('navigate', 'concerts')"
          aria-label="새 콘서트 추가"
        >
          <div class="quick-action-icon">🎵</div>
          <span>새 콘서트 추가</span>
        </button>
        
        <button 
          class="quick-action-btn" 
          @click="$emit('navigate', 'media')"
          aria-label="사진 업로드"
        >
          <div class="quick-action-icon">📸</div>
          <span>사진 업로드</span>
        </button>
        
        <button 
          class="quick-action-btn" 
          @click="$emit('navigate', 'media')"
          aria-label="음악 추가"
        >
          <div class="quick-action-icon">🎬</div>
          <span>음악 추가</span>
        </button>
      </div>
    </div>

    <!-- 시스템 상태 -->
    <div class="dashboard-section">
      <h2>시스템 상태</h2>
      <div class="system-status">
        <div class="status-item">
          <span class="status-label">데이터베이스 연결</span>
          <span class="status-indicator success" aria-label="정상">●</span>
          <span class="status-text">정상</span>
        </div>
        <div class="status-item">
          <span class="status-label">파일 업로드</span>
          <span class="status-indicator success" aria-label="정상">●</span>
          <span class="status-text">정상</span>
        </div>
        <div class="status-item">
          <span class="status-label">API 서버</span>
          <span class="status-indicator success" aria-label="정상">●</span>
          <span class="status-text">정상</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  concerts: {
    type: Array,
    default: () => []
  },
  photoGroups: {
    type: Array,
    default: () => []
  },
  musicList: {
    type: Array,
    default: () => []
  },
  videos: {
    type: Array,
    default: () => []
  },
  newsList: {
    type: Array,
    default: () => []
  },
  equipmentList: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['navigate'])

// Computed
const totalMediaCount = computed(() => 
  props.musicList.length + props.videos.length + props.newsList.length + props.equipmentList.length
)

const upcomingConcertsCount = computed(() => 
  props.concerts.filter(c => !c.isPast).length
)

const pastConcertsCount = computed(() => 
  props.concerts.filter(c => c.isPast).length
)

const totalPhotosCount = computed(() => 
  props.photoGroups.reduce((total, group) => total + (group.photos ? group.photos.length : 0), 0)
)
</script>

<style scoped>
.dashboard-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 2rem;
  color: #333;
}

/* Dashboard Styles */
.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(15rem, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
  width: 4rem;
  height: 4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 50%;
}

.stat-content h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.1rem;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #3498db;
  margin: 0 0 0.25rem 0;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
  margin: 0;
}

.dashboard-section h2 {
  margin: 0 0 1.5rem 0;
  color: #333;
  font-size: 1.3rem;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
}

.activity-icon {
  font-size: 1.5rem;
  width: 3rem;
  height: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
}

.activity-content h4 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1rem;
}

.activity-content p {
  margin: 0.25rem 0;
  color: #666;
  font-size: 0.9rem;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(12rem, 1fr));
  gap: 1rem;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1.5rem;
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  color: #333;
}

.quick-action-btn:hover {
  border-color: #3498db;
  background: #f8f9fa;
}

.quick-action-icon {
  font-size: 2rem;
}

.quick-action-btn span {
  font-weight: 500;
  font-size: 0.9rem;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 0.5rem;
}

.status-label {
  font-weight: 500;
  color: #333;
  min-width: 8rem;
}

.status-indicator {
  font-size: 1.2rem;
}

.status-indicator.success {
  color: #28a745;
}

.status-text {
  color: #666;
  font-size: 0.9rem;
}
</style>
