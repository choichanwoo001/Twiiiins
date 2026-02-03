<template>
  <div class="events-section">
    <div class="section-layout">
      <h2 class="section-title">{{ title }}</h2>
      <div class="events-list">
        <div class="event-item" v-for="event in events" :key="event.id">
          <div class="event-preview" @click="$emit('toggle', event.id)">
            <div class="event-date">
              <span class="date-main">{{ event.dateMain }}</span>
              <span class="date-year">{{ event.dateYear }}</span>
            </div>
            <div class="event-info">
              <div class="event-location">{{ event.location }}</div>
              <div class="event-name">{{ event.name }}</div>
            </div>
            <div class="event-expand" :class="{ 'expanded': event.expanded }">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
          
          <!-- 상세 정보 -->
          <div class="event-details" v-if="event.expanded">
            <div class="detail-row">
              <span class="detail-label">{{ isPast ? 'Concert started at:' : 'Concert starts at:' }}</span>
              <span class="detail-value">{{ event.startTime }}</span>
            </div>
            <div class="detail-row" v-if="!isPast">
              <span class="detail-label">Ticket Info:</span>
              <a :href="event.ticketInfo" target="_blank" class="detail-link">{{ event.ticketInfo }}</a>
            </div>
            <div class="detail-row">
              <span class="detail-label">Location:</span>
              <span class="detail-value">{{ event.fullLocation }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Google Map:</span>
              <a :href="event.googleMap" target="_blank" class="detail-link">View on Google Maps</a>
            </div>
            <div class="detail-row" v-if="event.collaborationInfo">
              <span class="detail-label">Concert information (only for collaboration):</span>
              <span class="detail-value">{{ event.collaborationInfo }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  events: {
    type: Array,
    required: true,
    default: () => []
  },
  isPast: {
    type: Boolean,
    default: false
  }
})

defineEmits(['toggle'])
</script>

<style scoped>
/* 이벤트 섹션 */
.events-section {
  margin-bottom: 3rem;
}

/* 섹션 레이아웃 (좌우 배치) */
.section-layout {
  display: grid;
  grid-template-columns: 12.5rem 1fr;
  gap: 2rem;
  align-items: start;
}

/* 섹션 제목 */
.section-title {
  font-size: 1.1rem;
  color: var(--color-text-tertiary); /* #666 */
  margin: 0;
  font-weight: 500;
  padding-top: 1.5rem;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.event-item {
  border-bottom: 0.0625rem solid var(--color-border); /* #e0e0e0 -> var(--color-border) */
  transition: background-color 0.3s ease;
}

.event-item:last-child {
  border-bottom: none;
}

.event-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.event-preview:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.event-date {
  font-size: 1rem;
  color: var(--color-text-secondary); /* #555 */
  min-width: 9.375rem;
  text-align: left;
  display: flex;
  flex-direction: column;
}

.date-main {
  display: inline;
}

.date-year {
  display: inline;
  margin-left: 0.25rem;
}

.event-info {
  flex: 1;
  text-align: left;
  margin-left: 2rem;
}

.event-location {
  font-size: 1rem;
  font-weight: 500;
  color: var(--color-text); /* #1E1D1D */
  margin-bottom: 0.25rem;
}

.event-name {
  font-size: 0.9rem;
  color: var(--color-text-tertiary); /* #666 */
}

.event-expand {
  color: var(--color-text-muted); /* #999 */
  transition: all 0.3s ease;
  min-width: 1.875rem;
  text-align: right;
}

.event-expand.expanded {
  transform: rotate(180deg);
}

.event-expand:hover {
  color: var(--color-text-tertiary); /* #666 */
}

/* 상세 정보 스타일 */
.event-details {
  padding: 1.5rem 0 2rem 0;
  background-color: #f8f8f8;
  border-top: 0.0625rem solid var(--color-border); /* #e0e0e0 */
}

.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 1rem;
  gap: 1rem;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 0.9rem;
  color: var(--color-text-tertiary); /* #666 */
  min-width: 12.5rem;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-value {
  font-size: 0.9rem;
  color: var(--color-text); /* #1E1D1D */
  flex: 1;
}

.detail-link {
  font-size: 0.9rem;
  color: var(--color-accent-red); /* #8B0000 */
  text-decoration: none;
  transition: color 0.2s ease;
}

.detail-link:hover {
  color: var(--color-accent-red-hover); /* #A00000 */
  text-decoration: underline;
}

/* 모바일 반응형 스타일 */
@media (max-width: 48rem) {
  .section-layout {
    display: flex;
    flex-direction: column; /* 세로 배치 */
    gap: 1rem;
  }

  .section-title {
    padding-top: 0;
    margin-bottom: 0.5rem;
    font-size: 1.2rem;
    font-weight: 300;
  }

  .event-preview {
    align-items: flex-start; /* 상단 정렬 */
    padding: 1.25rem 0;
  }

  .event-date {
    min-width: 4rem; /* 최소 너비 확보 */
    margin-right: 1rem;
    display: flex;
    flex-direction: column;
    white-space: nowrap;
  }

  .date-main {
    font-weight: 500;
    color: var(--color-text); /* #1E1D1D */
    font-size: 0.9rem; /* 폰트 크기 복구 */
    display: block;
  }

  .date-year {
    display: block; /* 줄바꿈 복구 */
    margin-left: 0;
    color: var(--color-text-lighter); /* #888 */
    font-size: 0.85rem;
    margin-top: 0.2rem;
  }

  .event-info {
    margin-left: 0;
    margin-right: 0.5rem;
    min-width: 0;
    flex: 1; /* 남은 공간 차지 */
  }
  
  .event-location {
    font-size: 0.95rem;
    white-space: normal; /* 줄바꿈 허용 */
    overflow: visible; /* 잘림 방지 */
    text-overflow: clip; /* ... 제거 */
    line-height: 1.3;
    margin-bottom: 0.2rem;
  }

  .event-name {
    white-space: normal; /* 줄바꿈 허용 */
    overflow: visible; /* 잘림 방지 */
    text-overflow: clip; /* ... 제거 */
    line-height: 1.3;
  }

  /* 상세 정보 레이아웃 가로 배치에서 세로 배치로 변경 */
  .detail-row {
    flex-direction: column;
    gap: 0.25rem;
    margin-bottom: 0.75rem;
  }

  .detail-label {
    min-width: auto;
    width: 100%;
    font-size: 0.85rem;
    color: var(--color-text-lighter); /* #888 */
  }

  /* 모바일에서 상세 정보 텍스트 크기 조정 */
  .detail-value, .detail-link {
    font-size: 0.95rem; /* 가독성 위해 약간 키움 */
    word-break: break-all; /* 긴 URL 등 줄바꿈 */
    line-height: 1.4;
  }
}
</style>
